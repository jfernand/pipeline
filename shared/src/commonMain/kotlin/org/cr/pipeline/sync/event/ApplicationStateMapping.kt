/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.AttachmentSummary
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.FollowUpItem
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
    // Whether this reads "Applied" or "Created" has to key off status, not dateApplied's
    // presence — AddEditScreen pre-fills dateApplied to today for every new application
    // regardless of chosen status, so a fresh Wishlist entry has one too even though nothing's
    // been applied to yet. A creation date always exists either way — the reducer seeds
    // statusHistory's earliest entry at creation time (applyEvent, ApplicationCreated) — so
    // there's always a date to measure "ago" from, never a placeholder like the old "Saved" label.
    val created = statusHistory.minByOrNull { it.date }?.date ?: today
    val activity = if (status == AppStatus.WISHLIST) {
        "Created" to relativeDays(created, today)
    } else {
        "Applied" to relativeDays(dateApplied ?: created, today)
    }
    return JobApplication(
        id = id,
        company = company,
        role = role,
        status = status,
        daysAgo = daysAgo.toInt(),
        activity = activity,
        overdueDays = overdueDays?.toInt(),
        source = source,
        provenance = lastProvenance,
    )
}

private fun relativeDays(date: LocalDate, today: LocalDate): String {
    val days = (today.toEpochDays() - date.toEpochDays()).toInt()
    return if (days <= 0) "today" else "${days}d ago"
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
        contacts = contacts.map { ContactSummary(id = it.id.value, name = it.name, role = it.role, email = it.email) },
        reminders = reminders
            .sortedBy { it.dueDate }
            .map { reminder -> ReminderSummary(reminder.message, reminder.dueDate.formatShort(), reminder.dueDate < today) },
        attachments = attachments.map { AttachmentSummary(id = it.id.value, kind = it.kind, fileName = it.fileName) },
        provenance = lastProvenance,
    )
}

/**
 * Every overdue thing across every application — an overdue [ApplicationState.nextActionDate]
 * and every overdue [ReminderRecord] are independent signals here, same as they already are on
 * the list (the "Needs follow-up" section, driven by [toJobApplication]'s overdueDays) and on the
 * detail screen ([toApplicationDetail]'s reminders, via `OverdueBanner`) — this just flattens both
 * into one sorted list instead of requiring a screen per application to see them. Not
 * deduplicated when the two happen to share a date (e.g. a next-action date set to match a
 * reminder) for the same reason those two existing call sites don't cross-reference each other
 * either: they're recorded as separate facts, so both are shown.
 */
fun List<Pair<Long, ApplicationState>>.toFollowUpItems(today: LocalDate = todayDate()): List<FollowUpItem> {
    data class Raw(val id: Long, val company: String, val role: String, val message: String, val dueDate: LocalDate)

    return flatMap { (id, state) ->
        val nextAction = state.nextActionDate
            ?.takeIf { it < today }
            ?.let { Raw(id, state.company, state.role, "Next action due", it) }
        val reminders = state.reminders
            .filter { it.dueDate < today }
            .map { reminder -> Raw(id, state.company, state.role, reminder.message, reminder.dueDate) }
        listOfNotNull(nextAction) + reminders
    }
        .sortedBy { it.dueDate }
        .map { FollowUpItem(it.id, it.company, it.role, it.message, it.dueDate.formatShort()) }
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
