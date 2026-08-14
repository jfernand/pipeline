package org.cr.pipeline.model

data class ApplicationDetail(
    val id: Long,
    val company: String,
    val role: String,
    val status: AppStatus,
    val daysSinceActivity: Int,
    val source: String?,
    val dateApplied: String?,
    val postingUrl: String?,
    val notes: String,
    val statusHistory: List<StatusHistoryEntry>,
    val contacts: List<ContactSummary>,
    val reminders: List<ReminderSummary>,
)

/** Oldest first; [current] marks the most recent entry (the application's present status). */
data class StatusHistoryEntry(
    val status: AppStatus,
    val date: String,
    val note: String,
    val current: Boolean,
)

data class ContactSummary(
    val name: String,
    val role: String,
    val email: String,
)

data class ReminderSummary(
    val message: String,
    val dueDate: String,
    val overdue: Boolean,
)
