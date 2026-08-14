package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.cr.pipeline.data.db.Application as ApplicationEntity
import org.cr.pipeline.data.db.ApplicationDao
import org.cr.pipeline.data.db.ApplicationStatus
import org.cr.pipeline.data.db.ApplicationWithDetails
import org.cr.pipeline.data.db.Contact
import org.cr.pipeline.data.db.ContactDao
import org.cr.pipeline.data.db.Reminder
import org.cr.pipeline.data.db.ReminderDao
import org.cr.pipeline.data.db.StatusEvent
import org.cr.pipeline.data.db.StatusEventDao
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.ReminderSummary
import org.cr.pipeline.model.StatusHistoryEntry
import org.cr.pipeline.model.formatShort
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.todayDate

/** Room-backed on Android/JVM/iOS. Seeds [org.cr.pipeline.model.seedApplications] on first run,
 *  so the app has real, browsable data to QA before any application is added by hand. */
internal class RoomJobApplicationRepository(
    private val applicationDao: ApplicationDao,
    private val statusEventDao: StatusEventDao,
    private val contactDao: ContactDao,
    private val reminderDao: ReminderDao,
) : JobApplicationRepository {
    override fun observeApplications(): Flow<List<JobApplication>> = flow {
        seedIfEmpty()
        emitAll(applicationDao.observeAll().map { entities -> entities.map { it.toUiModel() } })
    }

    override fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?> =
        applicationDao.observeWithDetails(id).map { it?.toDetailUiModel() }

    private suspend fun seedIfEmpty() {
        if (applicationDao.count() > 0) return
        val today = todayDate()
        seedApplications.forEach { seed ->
            val applicationId = applicationDao.insert(
                ApplicationEntity(
                    companyName = seed.company,
                    role = seed.role,
                    status = seed.status.toDbStatus(),
                    dateApplied = today.minus(seed.daysAgoApplied, DateTimeUnit.DAY),
                    postingUrl = seed.postingUrl,
                    source = seed.source,
                    notes = seed.notes,
                    nextActionDate = seed.nextActionOffsetDays?.let { today.plus(it, DateTimeUnit.DAY) },
                ),
            )
            seed.statusHistory.forEach { event ->
                statusEventDao.insert(
                    StatusEvent(
                        applicationId = applicationId,
                        status = event.status.toDbStatus(),
                        date = today.minus(event.daysAgo, DateTimeUnit.DAY),
                        note = event.note,
                    ),
                )
            }
            seed.contacts.forEach { contact ->
                contactDao.insert(
                    Contact(
                        applicationId = applicationId,
                        name = contact.name,
                        role = contact.role,
                        email = contact.email,
                        linkedinUrl = null,
                    ),
                )
            }
            seed.reminders.forEach { reminder ->
                reminderDao.insert(
                    Reminder(
                        applicationId = applicationId,
                        dueDate = today.plus(reminder.offsetDays, DateTimeUnit.DAY),
                        message = reminder.message,
                    ),
                )
            }
        }
    }
}

private fun ApplicationEntity.toUiModel(): JobApplication {
    val today = todayDate()
    val daysAgo = dateApplied?.let { today.toEpochDays() - it.toEpochDays() } ?: 0
    val overdueDays = nextActionDate
        ?.takeIf { it < today }
        ?.let { today.toEpochDays() - it.toEpochDays() }
    val meta = dateApplied?.let { "Applied ${it.formatShort()}" } ?: "Saved"
    return JobApplication(
        id = id,
        company = companyName,
        role = role,
        status = status.toUiStatus(),
        daysAgo = daysAgo.toInt(),
        meta = meta,
        overdueDays = overdueDays?.toInt(),
    )
}

private fun ApplicationWithDetails.toDetailUiModel(): ApplicationDetail {
    val today = todayDate()
    val orderedHistory = statusHistory.sortedBy { it.date }
    return ApplicationDetail(
        id = application.id,
        company = application.companyName,
        role = application.role,
        status = application.status.toUiStatus(),
        daysSinceActivity = orderedHistory.lastOrNull()?.date
            ?.let { (today.toEpochDays() - it.toEpochDays()).toInt() }
            ?: 0,
        source = application.source,
        dateApplied = application.dateApplied?.formatShort(),
        postingUrl = application.postingUrl,
        notes = application.notes,
        statusHistory = orderedHistory.mapIndexed { index, event ->
            StatusHistoryEntry(
                status = event.status.toUiStatus(),
                date = event.date.formatShort(),
                note = event.note,
                current = index == orderedHistory.lastIndex,
            )
        },
        contacts = contacts.map { ContactSummary(name = it.name, role = it.role.orEmpty(), email = it.email.orEmpty()) },
        reminders = reminders
            .sortedBy { it.dueDate }
            .map { reminder ->
                ReminderSummary(
                    message = reminder.message,
                    dueDate = reminder.dueDate.formatShort(),
                    overdue = reminder.dueDate < today,
                )
            },
    )
}

private fun ApplicationStatus.toUiStatus(): AppStatus = when (this) {
    ApplicationStatus.WISHLIST -> AppStatus.WISHLIST
    ApplicationStatus.APPLIED -> AppStatus.APPLIED
    ApplicationStatus.PHONE_SCREEN -> AppStatus.SCREEN
    ApplicationStatus.INTERVIEWING -> AppStatus.INTERVIEW
    ApplicationStatus.OFFER -> AppStatus.OFFER
    ApplicationStatus.REJECTED -> AppStatus.REJECTED
    ApplicationStatus.WITHDRAWN -> AppStatus.WITHDRAWN
}

private fun AppStatus.toDbStatus(): ApplicationStatus = when (this) {
    AppStatus.WISHLIST -> ApplicationStatus.WISHLIST
    AppStatus.APPLIED -> ApplicationStatus.APPLIED
    AppStatus.SCREEN -> ApplicationStatus.PHONE_SCREEN
    AppStatus.INTERVIEW -> ApplicationStatus.INTERVIEWING
    AppStatus.OFFER -> ApplicationStatus.OFFER
    AppStatus.REJECTED -> ApplicationStatus.REJECTED
    AppStatus.WITHDRAWN -> ApplicationStatus.WITHDRAWN
}
