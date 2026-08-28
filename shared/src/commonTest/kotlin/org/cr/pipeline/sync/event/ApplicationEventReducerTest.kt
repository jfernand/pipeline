/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.AttachmentKind
import org.cr.pipeline.model.ContactInput
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationEventReducerTest {
    private val today = LocalDate(2026, 8, 16)
    private val applicationId = ApplicationId("app-1")

    // Every ApplicationInput field set to a distinctive, non-default value: if a future field
    // gets added here and to ApplicationState but never wired into applyEvent, the assertions
    // below that read it back off the reduced state will catch it.
    private val fullInput = ApplicationInput(
        company = "Cedar & Byrne",
        role = "Senior Mobile Engineer",
        status = AppStatus.INTERVIEW,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = LocalDate(2026, 8, 20),
        postingUrl = "cedarandbyrne.com/careers/senior-mobile",
        source = "LinkedIn",
        notes = "Recruiter mentioned a fast timeline.",
    )

    @Test
    fun `ApplicationCreated populates every ApplicationInput field onto the new state`() {
        val state = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)

        assertEquals(fullInput.company, state.company)
        assertEquals(fullInput.role, state.role)
        assertEquals(fullInput.status, state.status)
        assertEquals(fullInput.dateApplied, state.dateApplied)
        assertEquals(fullInput.nextActionDate, state.nextActionDate)
        assertEquals(fullInput.postingUrl, state.postingUrl)
        assertEquals(fullInput.source, state.source)
        assertEquals(fullInput.notes, state.notes)
        assertEquals(
            listOf(StatusHistoryRecord(fullInput.status, fullInput.dateApplied!!, "Application created")),
            state.statusHistory,
        )
        assertEquals(emptyList(), state.contacts)
        assertEquals(emptyList(), state.reminders)
        assertEquals(emptyList(), state.attachments)
    }

    @Test
    fun `ApplicationCreated without a dateApplied dates the first history entry today`() {
        val state = applyEvent(null, ApplicationCreated(applicationId, fullInput.copy(dateApplied = null)), today)

        assertEquals(today, state.statusHistory.single().date)
    }

    @Test
    fun `ApplicationEdited overwrites every field with the new input`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val newInput = ApplicationInput(
            company = "Northwind Labs",
            role = "Staff Android Engineer",
            status = AppStatus.INTERVIEW, // unchanged: shouldn't append a history entry
            dateApplied = LocalDate(2026, 8, 2),
            nextActionDate = LocalDate(2026, 8, 25),
            postingUrl = "northwindlabs.com/careers/staff-android",
            source = "Referral",
            notes = "Completely different notes.",
        )

        val edited = applyEvent(created, ApplicationEdited(applicationId, newInput), today)

        assertEquals(newInput.company, edited.company)
        assertEquals(newInput.role, edited.role)
        assertEquals(newInput.status, edited.status)
        assertEquals(newInput.dateApplied, edited.dateApplied)
        assertEquals(newInput.nextActionDate, edited.nextActionDate)
        assertEquals(newInput.postingUrl, edited.postingUrl)
        assertEquals(newInput.source, edited.source)
        assertEquals(newInput.notes, edited.notes)
        assertEquals(created.statusHistory, edited.statusHistory)
    }

    @Test
    fun `ApplicationEdited appends a history entry only when status actually changes`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)

        val edited = applyEvent(created, ApplicationEdited(applicationId, fullInput.copy(status = AppStatus.OFFER)), today)

        assertEquals(2, edited.statusHistory.size)
        assertEquals(StatusHistoryRecord(AppStatus.OFFER, today, "Updated via edit"), edited.statusHistory.last())
    }

    @Test
    fun `StatusChanged updates status and appends a history entry without touching other fields`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)

        val changed = applyEvent(created, StatusChanged(applicationId, AppStatus.OFFER, "Verbal offer on the call"), today)

        assertEquals(AppStatus.OFFER, changed.status)
        assertEquals(StatusHistoryRecord(AppStatus.OFFER, today, "Verbal offer on the call"), changed.statusHistory.last())
        assertEquals(created.company, changed.company)
        assertEquals(created.role, changed.role)
        assertEquals(created.dateApplied, changed.dateApplied)
        assertEquals(created.nextActionDate, changed.nextActionDate)
        assertEquals(created.postingUrl, changed.postingUrl)
        assertEquals(created.source, changed.source)
        assertEquals(created.notes, changed.notes)
    }

    @Test
    fun `ContactAdded appends a contact carrying its event's contactId, without touching other fields`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val contactId = ContactId("contact-1")
        val contact = ContactInput("Dana Whitfield", "Engineering manager", "dana@northwindlabs.com")

        val withContact = applyEvent(created, ContactAdded(applicationId, contactId, contact), today)

        assertEquals(
            listOf(ContactRecord(contactId, "Dana Whitfield", "Engineering manager", "dana@northwindlabs.com")),
            withContact.contacts,
        )
        assertEquals(created.company, withContact.company)
        assertEquals(created.status, withContact.status)
        assertEquals(created.statusHistory, withContact.statusHistory)
    }

    @Test
    fun `ContactAdded appends to, rather than replaces, existing contacts`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val first = applyEvent(
            created,
            ContactAdded(applicationId, ContactId("contact-1"), ContactInput("Dana Whitfield", "Engineering manager", "dana@northwindlabs.com")),
            today,
        )

        val second = applyEvent(
            first,
            ContactAdded(applicationId, ContactId("contact-2"), ContactInput("Marcus Oyelaran", "Recruiter", "marcus@northwindlabs.com")),
            today,
        )

        assertEquals(2, second.contacts.size)
        assertEquals("Dana Whitfield", second.contacts.first().name)
        assertEquals("Marcus Oyelaran", second.contacts.last().name)
        assertEquals(ContactId("contact-1"), second.contacts.first().id)
        assertEquals(ContactId("contact-2"), second.contacts.last().id)
    }

    @Test
    fun `FileAttached appends a MISC attachment without touching other fields`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val attachmentId = AttachmentId("attachment-1")

        val withAttachment = applyEvent(created, FileAttached(applicationId, attachmentId, "notes.pdf"), today)

        assertEquals(
            listOf(AttachmentRecord(attachmentId, AttachmentKind.MISC, "notes.pdf")),
            withAttachment.attachments,
        )
        assertEquals(created.company, withAttachment.company)
        assertEquals(created.statusHistory, withAttachment.statusHistory)
    }

    @Test
    fun `FileAttached appends, rather than replaces, additional MISC attachments`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val first = applyEvent(created, FileAttached(applicationId, AttachmentId("a1"), "one.pdf"), today)

        val second = applyEvent(first, FileAttached(applicationId, AttachmentId("a2"), "two.pdf"), today)

        assertEquals(2, second.attachments.size)
        assertEquals(listOf("one.pdf", "two.pdf"), second.attachments.map { it.fileName })
    }

    @Test
    fun `ResumeAttached replaces the prior resume instead of appending`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val first = applyEvent(created, ResumeAttached(applicationId, AttachmentId("r1"), "resume-v1.pdf"), today)

        val second = applyEvent(first, ResumeAttached(applicationId, AttachmentId("r2"), "resume-v2.pdf"), today)

        assertEquals(
            listOf(AttachmentRecord(AttachmentId("r2"), AttachmentKind.RESUME, "resume-v2.pdf")),
            second.attachments,
        )
    }

    @Test
    fun `CoverLetterAttached replaces the prior cover letter instead of appending`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val first = applyEvent(created, CoverLetterAttached(applicationId, AttachmentId("c1"), "cover-v1.pdf"), today)

        val second = applyEvent(first, CoverLetterAttached(applicationId, AttachmentId("c2"), "cover-v2.pdf"), today)

        assertEquals(
            listOf(AttachmentRecord(AttachmentId("c2"), AttachmentKind.COVER_LETTER, "cover-v2.pdf")),
            second.attachments,
        )
    }

    @Test
    fun `ResumeAttached doesn't disturb an existing COVER_LETTER slot`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val withCoverLetter = applyEvent(created, CoverLetterAttached(applicationId, AttachmentId("c1"), "cover.pdf"), today)

        val withResumeToo = applyEvent(withCoverLetter, ResumeAttached(applicationId, AttachmentId("r1"), "resume.pdf"), today)

        assertEquals(2, withResumeToo.attachments.size)
        assertEquals(setOf(AttachmentKind.COVER_LETTER, AttachmentKind.RESUME), withResumeToo.attachments.map { it.kind }.toSet())
    }

    @Test
    fun `AttachmentRemoved removes only the matching attachment`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val withTwo = applyEvent(created, FileAttached(applicationId, AttachmentId("a1"), "one.pdf"), today)
            .let { applyEvent(it, FileAttached(applicationId, AttachmentId("a2"), "two.pdf"), today) }

        val removed = applyEvent(withTwo, AttachmentRemoved(applicationId, AttachmentId("a1")), today)

        assertEquals(listOf(AttachmentRecord(AttachmentId("a2"), AttachmentKind.MISC, "two.pdf")), removed.attachments)
    }

    @Test
    fun `AttachmentRemoved for an unknown id is a no-op`() {
        val created = applyEvent(null, ApplicationCreated(applicationId, fullInput), today)
        val withOne = applyEvent(created, FileAttached(applicationId, AttachmentId("a1"), "one.pdf"), today)

        val removed = applyEvent(withOne, AttachmentRemoved(applicationId, AttachmentId("does-not-exist")), today)

        assertEquals(withOne.attachments, removed.attachments)
    }
}
