package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.ReminderSummary
import org.cr.pipeline.model.StatusHistoryEntry
import org.cr.pipeline.model.formatShort
import org.cr.pipeline.model.todayDate

/**
 * The other direction from [applyEvent]: turning stored state into what the UI reads. Lives here,
 * shared, for the same reason the reducer does — every [org.cr.pipeline.data.ApplicationStateStore]
 * implementation used to hand-map these independently, which is exactly the kind of duplication
 * that lets a field silently stop propagating.
 *
 * [today] is a parameter rather than read internally so these stay pure/testable; it defaults to
 * the real clock for callers that don't care.
 */
fun ApplicationState.toJobApplication(id: Long, today: LocalDate = todayDate()): JobApplication {
    val daysAgo = dateApplied?.let { today.toEpochDays() - it.toEpochDays() } ?: 0
    val overdueDays = nextActionDate?.takeIf { it < today }?.let { today.toEpochDays() - it.toEpochDays() }
    val meta = dateApplied?.let { "Applied ${it.formatShort()}" } ?: "Saved"
    return JobApplication(
        id = id,
        company = company,
        role = role,
        status = status,
        daysAgo = daysAgo.toInt(),
        meta = meta,
        overdueDays = overdueDays?.toInt(),
    )
}

fun ApplicationState.toApplicationDetail(id: Long, today: LocalDate = todayDate()): ApplicationDetail {
    val orderedHistory = statusHistory.sortedBy { it.date }
    return ApplicationDetail(
        id = id,
        company = company,
        role = role,
        status = status,
        daysSinceActivity = orderedHistory.lastOrNull()?.date
            ?.let { (today.toEpochDays() - it.toEpochDays()).toInt() }
            ?: 0,
        source = source,
        dateApplied = dateApplied?.formatShort(),
        postingUrl = postingUrl,
        notes = notes,
        statusHistory = orderedHistory.mapIndexed { index, event ->
            StatusHistoryEntry(
                status = event.status,
                date = event.date.formatShort(),
                note = event.note,
                current = index == orderedHistory.lastIndex,
            )
        },
        contacts = contacts.map { ContactSummary(name = it.name, role = it.role, email = it.email) },
        reminders = reminders
            .sortedBy { it.dueDate }
            .map { reminder -> ReminderSummary(reminder.message, reminder.dueDate.formatShort(), reminder.dueDate < today) },
    )
}

fun ApplicationState.toApplicationInput(): ApplicationInput = ApplicationInput(
    company = company,
    role = role,
    status = status,
    dateApplied = dateApplied,
    nextActionDate = nextActionDate,
    postingUrl = postingUrl,
    source = source,
    notes = notes,
)
