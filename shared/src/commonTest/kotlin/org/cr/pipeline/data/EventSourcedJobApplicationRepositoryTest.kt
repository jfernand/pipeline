/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.cr.pipeline.data.io.FakeFileArchiveService
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.ContactInput
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationDeleted
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.AttachmentRemoved
import org.cr.pipeline.sync.event.ContactAdded
import org.cr.pipeline.sync.event.CoverLetterAttached
import org.cr.pipeline.sync.event.EventProvenance
import org.cr.pipeline.sync.event.FileAttached
import org.cr.pipeline.sync.event.InMemoryEventLog
import org.cr.pipeline.sync.event.ResumeAttached
import org.cr.pipeline.sync.event.StatusChanged
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EventSourcedJobApplicationRepositoryTest {
    private val input = ApplicationInput(
        company = "Cedar & Byrne",
        role = "Senior Mobile Engineer",
        status = AppStatus.APPLIED,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = null,
        postingUrl = null,
        source = "LinkedIn",
        notes = "",
    )

    @Test
    fun `saveApplication with no id constructs the application and persists it`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())

        val id = repository.saveApplication(null, input)

        val state = store.states.getValue(id)
        assertEquals(input.company, state.company)
        assertEquals(input.status, state.status)
        assertEquals("Application created", state.statusHistory.single().note)
    }

    @Test
    fun `saveApplication with an existing id edits in place through the same reducer`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        repository.saveApplication(id, input.copy(company = "Northwind Labs", status = AppStatus.OFFER))

        val state = store.states.getValue(id)
        assertEquals("Northwind Labs", state.company)
        assertEquals(AppStatus.OFFER, state.status)
        assertEquals(2, state.statusHistory.size)
    }

    @Test
    fun `every event appended for one application carries the same applicationId as its create`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog)
        val id = repository.saveApplication(null, input)

        repository.saveApplication(id, input.copy(company = "Northwind Labs"))
        repository.updateStatus(id, AppStatus.OFFER, "Verbal offer")

        val applicationIds = eventLog.observeChain().first()
            .map { Json.decodeFromString<ApplicationEvent>(it.payload).applicationId }
        assertEquals(1, applicationIds.toSet().size, "create, edit, and status-change should all reference the same applicationId")
    }

    @Test
    fun `updateStatus appends a history entry with its note`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        repository.updateStatus(id, AppStatus.INTERVIEW, "Recruiter screen went well")

        val state = store.states.getValue(id)
        assertEquals(AppStatus.INTERVIEW, state.status)
        assertEquals("Recruiter screen went well", state.statusHistory.last().note)
    }

    @Test
    fun `updateStatus on an unknown id is a no-op`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())

        repository.updateStatus(999, AppStatus.OFFER, "shouldn't happen")

        assertEquals(emptyMap(), store.states)
    }

    @Test
    fun `addContact appends a contact and persists it`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        repository.addContact(id, ContactInput("Priya Nathan", "Recruiter", "priya.nathan@cedarandbyrne.com"))

        val state = store.states.getValue(id)
        assertEquals(1, state.contacts.size)
        assertEquals("Priya Nathan", state.contacts.single().name)
    }

    @Test
    fun `addContact on an unknown id is a no-op`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())

        repository.addContact(999, ContactInput("Priya Nathan", "Recruiter", "priya.nathan@cedarandbyrne.com"))

        assertEquals(emptyMap(), store.states)
    }

    @Test
    fun `addContact appends a correctly chained ContactAdded envelope`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog)
        val id = repository.saveApplication(null, input)

        repository.addContact(id, ContactInput("Priya Nathan", "Recruiter", "priya.nathan@cedarandbyrne.com"))

        val chain = eventLog.observeChain().first()
        assertEquals(2, chain.size)
        assertIs<ContactAdded>(Json.decodeFromString<ApplicationEvent>(chain[1].payload))
    }

    @Test
    fun `attachFile writes to the file service and persists a FileAttached event`() = runTest {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog(), fileService)
        val id = repository.saveApplication(null, input)

        repository.attachFile(id, "notes.pdf", "pdf bytes".encodeToByteArray())

        val state = store.states.getValue(id)
        assertEquals(1, state.attachments.size)
        assertEquals("notes.pdf", state.attachments.single().fileName)
        assertEquals(1, fileService.writes.size)
        assertEquals("notes.pdf", fileService.writes.single().fileName)
    }

    @Test
    fun `attachFile on an unknown id is a no-op`() = runTest {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog(), fileService)

        repository.attachFile(999, "notes.pdf", "pdf bytes".encodeToByteArray())

        assertEquals(emptyMap(), store.states)
        assertTrue(fileService.writes.isEmpty())
    }

    @Test
    fun `attachResume deletes the prior resume from the file service first`() = runTest {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog(), fileService)
        val id = repository.saveApplication(null, input)
        repository.attachResume(id, "resume-v1.pdf", "v1".encodeToByteArray())
        val firstAttachmentId = store.states.getValue(id).attachments.single().id

        repository.attachResume(id, "resume-v2.pdf", "v2".encodeToByteArray())

        assertEquals(listOf(firstAttachmentId), fileService.deletes.map { it.second })
        assertEquals("resume-v2.pdf", store.states.getValue(id).attachments.single().fileName)
    }

    @Test
    fun `attachCoverLetter deletes the prior cover letter from the file service first`() = runTest {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog(), fileService)
        val id = repository.saveApplication(null, input)
        repository.attachCoverLetter(id, "cover-v1.pdf", "v1".encodeToByteArray())
        val firstAttachmentId = store.states.getValue(id).attachments.single().id

        repository.attachCoverLetter(id, "cover-v2.pdf", "v2".encodeToByteArray())

        assertEquals(listOf(firstAttachmentId), fileService.deletes.map { it.second })
        assertEquals("cover-v2.pdf", store.states.getValue(id).attachments.single().fileName)
    }

    @Test
    fun `attachResume, attachCoverLetter, and attachFile each append a correctly chained event`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog, FakeFileArchiveService())
        val id = repository.saveApplication(null, input)

        repository.attachResume(id, "resume.pdf", "resume bytes".encodeToByteArray())
        repository.attachCoverLetter(id, "cover.pdf", "cover bytes".encodeToByteArray())
        repository.attachFile(id, "notes.pdf", "notes bytes".encodeToByteArray())

        val chain = eventLog.observeChain().first()
        assertEquals(4, chain.size)
        assertIs<ResumeAttached>(Json.decodeFromString<ApplicationEvent>(chain[1].payload))
        assertIs<CoverLetterAttached>(Json.decodeFromString<ApplicationEvent>(chain[2].payload))
        assertIs<FileAttached>(Json.decodeFromString<ApplicationEvent>(chain[3].payload))
    }

    @Test
    fun `removeAttachment deletes from the file service and persists an AttachmentRemoved event`() = runTest {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(store, eventLog, fileService)
        val id = repository.saveApplication(null, input)
        repository.attachFile(id, "notes.pdf", "pdf bytes".encodeToByteArray())
        val attachmentId = store.states.getValue(id).attachments.single().id

        repository.removeAttachment(id, attachmentId.value)

        assertEquals(emptyList(), store.states.getValue(id).attachments)
        assertEquals(listOf(attachmentId), fileService.deletes.map { it.second })
        val chain = eventLog.observeChain().first()
        assertIs<AttachmentRemoved>(Json.decodeFromString<ApplicationEvent>(chain.last().payload))
    }

    @Test
    fun `removeAttachment for an unknown attachmentId is a no-op`() = runTest {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog(), fileService)
        val id = repository.saveApplication(null, input)
        repository.attachFile(id, "notes.pdf", "pdf bytes".encodeToByteArray())

        repository.removeAttachment(id, "does-not-exist")

        assertEquals(1, store.states.getValue(id).attachments.size)
        assertTrue(fileService.deletes.isEmpty())
    }

    @Test
    fun `deleteApplication marks the state deleted and persists an ApplicationDeleted event`() = runTest {
        val store = FakeApplicationStateStore()
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(store, eventLog)
        val id = repository.saveApplication(null, input)

        repository.deleteApplication(id)

        assertTrue(store.states.getValue(id).deleted)
        val chain = eventLog.observeChain().first()
        assertIs<ApplicationDeleted>(Json.decodeFromString<ApplicationEvent>(chain.last().payload))
    }

    @Test
    fun `deleteApplication for an unknown id is a no-op`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())

        repository.deleteApplication(404)

        assertEquals(emptyMap(), store.states)
    }

    @Test
    fun `deleteApplication twice is a no-op the second time`() = runTest {
        val store = FakeApplicationStateStore()
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(store, eventLog)
        val id = repository.saveApplication(null, input)
        repository.deleteApplication(id)
        val chainLengthAfterFirstDelete = eventLog.observeChain().first().size

        repository.deleteApplication(id)

        assertEquals(chainLengthAfterFirstDelete, eventLog.observeChain().first().size)
    }

    @Test
    fun `a deleted application no longer appears in observeApplications, observeApplicationDetail, observeFollowUps, or getApplicationInput`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val overdue = input.copy(nextActionDate = LocalDate(2020, 1, 1))
        val id = repository.saveApplication(null, overdue)
        // Sanity check it's visible everywhere before deleting, so the assertions below actually
        // prove deletion hid it rather than it never having been visible in the first place.
        assertEquals(1, repository.observeApplications().first().size)
        assertEquals(1, repository.observeFollowUps().first().size)

        repository.deleteApplication(id)

        assertEquals(emptyList(), repository.observeApplications().first())
        assertNull(repository.observeApplicationDetail(id).first())
        assertEquals(emptyList(), repository.observeFollowUps().first())
        assertNull(repository.getApplicationInput(id))
    }

    @Test
    fun `observeApplicationDetail reflects the stored state and is null when nothing is stored`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        val detail = repository.observeApplicationDetail(id).first()
        assertEquals(input.company, detail?.company)
        assertNull(repository.observeApplicationDetail(404).first())
    }

    @Test
    fun `every mutation appends a correctly chained envelope to the event log`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog)

        val id = repository.saveApplication(null, input)
        repository.updateStatus(id, AppStatus.OFFER, "Verbal offer")

        val chain = eventLog.observeChain().first()
        assertEquals(2, chain.size)
        assertEquals(emptyList(), chain[0].parentHashes)
        assertEquals(listOf(chain[0].hash), chain[1].parentHashes)
        assertIs<ApplicationCreated>(Json.decodeFromString<ApplicationEvent>(chain[0].payload))
        assertIs<StatusChanged>(Json.decodeFromString<ApplicationEvent>(chain[1].payload))
    }

    @Test
    fun `saveApplication and updateStatus default to this device's own Device provenance`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog)
        val expected = EventProvenance.Device(eventLog.deviceId())

        val id = repository.saveApplication(null, input)
        repository.updateStatus(id, AppStatus.OFFER, "Verbal offer")

        val chain = eventLog.observeChain().first()
        assertEquals(expected, Json.decodeFromString<ApplicationEvent>(chain[0].payload).provenance)
        assertEquals(expected, Json.decodeFromString<ApplicationEvent>(chain[1].payload).provenance)
    }

    @Test
    fun `saveApplication and updateStatus record an explicit provenance`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog)
        val mcp = EventProvenance.McpClient("mcp")

        val id = repository.saveApplication(null, input, mcp)
        repository.updateStatus(id, AppStatus.OFFER, "Verbal offer", mcp)

        val chain = eventLog.observeChain().first()
        assertEquals(mcp, Json.decodeFromString<ApplicationEvent>(chain[0].payload).provenance)
        assertEquals(mcp, Json.decodeFromString<ApplicationEvent>(chain[1].payload).provenance)
    }
}
