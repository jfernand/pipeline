package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
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
}
