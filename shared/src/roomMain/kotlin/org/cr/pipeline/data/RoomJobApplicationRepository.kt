package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
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
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.ReminderSummary
import org.cr.pipeline.model.StatusHistoryEntry
import org.cr.pipeline.model.formatShort
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationEdited
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.ContactRecord
import org.cr.pipeline.sync.event.ReminderRecord
import org.cr.pipeline.sync.event.StatusChanged
import org.cr.pipeline.sync.event.StatusHistoryRecord
import org.cr.pipeline.sync.event.applyEvent

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

    override suspend fun getApplicationInput(id: Long): ApplicationInput? =
        applicationDao.getById(id)?.let { entity ->
            ApplicationInput(
                company = entity.companyName,
                role = entity.role,
                status = entity.status.toUiStatus(),
                dateApplied = entity.dateApplied,
                nextActionDate = entity.nextActionDate,
                postingUrl = entity.postingUrl,
                source = entity.source,
                notes = entity.notes,
            )
        }

    override suspend fun saveApplication(id: Long?, input: ApplicationInput): Long {
        if (id == null) {
            val newId = applicationDao.insert(
                ApplicationEntity(
                    companyName = input.company,
                    role = input.role,
                    status = input.status.toDbStatus(),
                    dateApplied = input.dateApplied,
                    postingUrl = input.postingUrl,
                    source = input.source,
                    notes = input.notes,
                    nextActionDate = input.nextActionDate,
                ),
            )
            // Row ids aren't stable across devices; this stands in until real ids are wired up.
            val state = applyEvent(null, ApplicationCreated(ApplicationId(newId.toString()), input), todayDate())
            insertNewStatusHistory(newId, sinceCount = 0, state)
            return newId
        }
        val existingDetails = applicationDao.observeWithDetails(id).first() ?: return id
        val currentState = existingDetails.toApplicationState()
        val newState = applyEvent(currentState, ApplicationEdited(ApplicationId(id.toString()), input), todayDate())
        applicationDao.update(
            existingDetails.application.copy(
                companyName = newState.company,
                role = newState.role,
                status = newState.status.toDbStatus(),
                dateApplied = newState.dateApplied,
                postingUrl = newState.postingUrl,
                source = newState.source,
                notes = newState.notes,
                nextActionDate = newState.nextActionDate,
            ),
        )
        insertNewStatusHistory(id, currentState.statusHistory.size, newState)
        return id
    }

    override suspend fun updateStatus(id: Long, status: AppStatus, note: String) {
        val existingDetails = applicationDao.observeWithDetails(id).first() ?: return
        val currentState = existingDetails.toApplicationState()
        val newState = applyEvent(currentState, StatusChanged(ApplicationId(id.toString()), status, note), todayDate())
        applicationDao.update(existingDetails.application.copy(status = newState.status.toDbStatus()))
        insertNewStatusHistory(id, currentState.statusHistory.size, newState)
    }

    /** [applyEvent] only ever appends status-history entries, never edits or removes past ones,
     *  so anything past index [sinceCount] in [newState] is new and needs inserting. */
    private suspend fun insertNewStatusHistory(applicationId: Long, sinceCount: Int, newState: ApplicationState) {
        newState.statusHistory.drop(sinceCount).forEach { entry ->
            statusEventDao.insert(
                StatusEvent(applicationId = applicationId, status = entry.status.toDbStatus(), date = entry.date, note = entry.note),
            )
        }
    }

    private fun ApplicationWithDetails.toApplicationState(): ApplicationState = ApplicationState(
        company = application.companyName,
        role = application.role,
        status = application.status.toUiStatus(),
        dateApplied = application.dateApplied,
        nextActionDate = application.nextActionDate,
        postingUrl = application.postingUrl,
        source = application.source,
        notes = application.notes,
        statusHistory = statusHistory.sortedBy { it.date }.map { StatusHistoryRecord(it.status.toUiStatus(), it.date, it.note) },
        contacts = contacts.map { ContactRecord(it.name, it.role.orEmpty(), it.email.orEmpty()) },
        reminders = reminders.map { ReminderRecord(it.message, it.dueDate) },
    )

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
