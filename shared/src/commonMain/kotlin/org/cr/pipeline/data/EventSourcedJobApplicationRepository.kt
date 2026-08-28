/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.cr.pipeline.data.io.FileArchiveService
import org.cr.pipeline.data.io.UnsupportedFileArchiveService
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.model.ContactInput
import org.cr.pipeline.model.FollowUpItem
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationEdited
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.AttachmentId
import org.cr.pipeline.sync.event.AttachmentRemoved
import org.cr.pipeline.sync.event.ContactAdded
import org.cr.pipeline.sync.event.ContactId
import org.cr.pipeline.sync.event.CoverLetterAttached
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.EventProvenance
import org.cr.pipeline.sync.event.FileAttached
import org.cr.pipeline.sync.event.ResumeAttached
import org.cr.pipeline.sync.event.StatusChanged
import org.cr.pipeline.sync.event.applyEvent
import org.cr.pipeline.sync.event.toApplicationDetail
import org.cr.pipeline.sync.event.toApplicationInput
import org.cr.pipeline.sync.event.toFollowUpItems
import org.cr.pipeline.sync.event.toJobApplication

/**
 * The single place that turns a UI-facing mutation into an [ApplicationEvent] and folds it
 * through [applyEvent] — [store] just persists the resulting state, [eventLog] just hashes and
 * appends the event itself. This is what keeps event construction from being duplicated per
 * storage backend the same way field-mapping used to be.
 */
class EventSourcedJobApplicationRepository(
    private val store: ApplicationStateStore,
    private val eventLog: EventLog,
    private val fileService: FileArchiveService = UnsupportedFileArchiveService,
) : JobApplicationRepository {
    override fun observeApplications(): Flow<List<JobApplication>> =
        store.observeAll().map { list -> list.map { (id, state) -> state.toJobApplication(id) } }

    override fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?> =
        store.observeState(id).map { it?.toApplicationDetail(id) }

    override fun observeFollowUps(): Flow<List<FollowUpItem>> =
        store.observeAll().map { it.toFollowUpItems() }

    override suspend fun getApplicationInput(id: Long): ApplicationInput? =
        store.getState(id)?.toApplicationInput()

    override suspend fun saveApplication(id: Long?, input: ApplicationInput, provenance: EventProvenance?): Long {
        if (id == null) {
            val event = ApplicationCreated(ApplicationId.random(), input, resolveProvenance(provenance))
            return persist(null, null, event)
        }
        val current = store.getState(id) ?: return id
        // Reuses the applicationId the original ApplicationCreated event carries, read off the
        // current state, rather than synthesizing one from id — every event for one application
        // has to share the same applicationId for replay to be able to group them back together.
        val event = ApplicationEdited(current.applicationId, input, resolveProvenance(provenance))
        return persist(id, current, event)
    }

    override suspend fun updateStatus(id: Long, status: AppStatus, note: String, provenance: EventProvenance?) {
        val current = store.getState(id) ?: return
        persist(id, current, StatusChanged(current.applicationId, status, note, resolveProvenance(provenance)))
    }

    override suspend fun addContact(id: Long, contact: ContactInput, provenance: EventProvenance?) {
        val current = store.getState(id) ?: return
        persist(id, current, ContactAdded(current.applicationId, ContactId.random(), contact, resolveProvenance(provenance)))
    }

    override suspend fun attachResume(id: Long, fileName: String, bytes: ByteArray, provenance: EventProvenance?) =
        attachSingleSlot(id, AttachmentKind.RESUME, fileName, bytes, provenance) { applicationId, attachmentId, resolved ->
            ResumeAttached(applicationId, attachmentId, fileName, resolved)
        }

    override suspend fun attachCoverLetter(id: Long, fileName: String, bytes: ByteArray, provenance: EventProvenance?) =
        attachSingleSlot(id, AttachmentKind.COVER_LETTER, fileName, bytes, provenance) { applicationId, attachmentId, resolved ->
            CoverLetterAttached(applicationId, attachmentId, fileName, resolved)
        }

    override suspend fun attachFile(id: Long, fileName: String, bytes: ByteArray, provenance: EventProvenance?) {
        val current = store.getState(id) ?: return
        val attachmentId = AttachmentId.random()
        fileService.write(current.applicationId, attachmentId, AttachmentKind.MISC, fileName, bytes)
        persist(id, current, FileAttached(current.applicationId, attachmentId, fileName, resolveProvenance(provenance)))
    }

    // Shared by attachResume/attachCoverLetter: both are one-slot kinds, so both need the same
    // "delete the prior file before writing the new one" step — a single-slot kind's prior file
    // would otherwise sit orphaned in the archive forever. applyEvent drops it from
    // ApplicationState.attachments, but only this repository ever touches the archive itself, so
    // cleaning up the bytes is on it, not the (pure) reducer.
    private suspend fun attachSingleSlot(
        id: Long,
        kind: AttachmentKind,
        fileName: String,
        bytes: ByteArray,
        provenance: EventProvenance?,
        event: (ApplicationId, AttachmentId, EventProvenance) -> ApplicationEvent,
    ) {
        val current = store.getState(id) ?: return
        current.attachments.firstOrNull { it.kind == kind }?.let { fileService.delete(current.applicationId, it.id) }
        val attachmentId = AttachmentId.random()
        fileService.write(current.applicationId, attachmentId, kind, fileName, bytes)
        persist(id, current, event(current.applicationId, attachmentId, resolveProvenance(provenance)))
    }

    override suspend fun removeAttachment(id: Long, attachmentId: String, provenance: EventProvenance?) {
        val current = store.getState(id) ?: return
        val target = AttachmentId(attachmentId)
        if (current.attachments.none { it.id == target }) return
        fileService.delete(current.applicationId, target)
        persist(id, current, AttachmentRemoved(current.applicationId, target, resolveProvenance(provenance)))
    }

    // deviceId() is a storage round trip on first call (then cached by eventLog itself), so this
    // only pays that cost for the calls that actually end up needing it — an explicit provenance
    // (e.g. the MCP server's) skips it entirely.
    private suspend fun resolveProvenance(explicit: EventProvenance?): EventProvenance =
        explicit ?: EventProvenance.Device(eventLog.deviceId())

    private suspend fun persist(id: Long?, current: ApplicationState?, event: ApplicationEvent): Long {
        // Touches store first: if this is the store's very first access, that's what triggers its
        // (one-time) materialization from the event log — it needs to see the log as it stood
        // *before* this event, or its own materialization would replay this event too, on top of
        // the write() call below that's about to record it a second time.
        store.observeAll().first()
        eventLog.append(event, Clock.System.now().toEpochMilliseconds())
        return store.write(id, applyEvent(current, event, todayDate()))
    }
}
