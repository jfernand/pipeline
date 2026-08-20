/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

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
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.ContactRecord
import org.cr.pipeline.sync.event.ReminderRecord
import org.cr.pipeline.sync.event.StatusHistoryRecord

/** Room-backed on Android/JVM/iOS. Seeds [org.cr.pipeline.model.seedApplications] on first run,
 *  so the app has real, browsable data to QA before any application is added by hand. Pure
 *  storage: [EventSourcedJobApplicationRepository] owns every decision about what a mutation
 *  means, this class only persists the [ApplicationState] it's handed. */
internal class RoomApplicationStateStore(
    private val applicationDao: ApplicationDao,
    private val statusEventDao: StatusEventDao,
    private val contactDao: ContactDao,
    private val reminderDao: ReminderDao,
) : ApplicationStateStore {
    override fun observeAll(): Flow<List<Pair<Long, ApplicationState>>> = flow {
        seedIfEmpty()
        emitAll(applicationDao.observeAllWithDetails().map { list -> list.map { it.application.id to it.toApplicationState() } })
    }

    override fun observeState(id: Long): Flow<ApplicationState?> =
        applicationDao.observeWithDetails(id).map { it?.toApplicationState() }

    override suspend fun getState(id: Long): ApplicationState? =
        applicationDao.observeWithDetails(id).first()?.toApplicationState()

    override suspend fun write(id: Long?, state: ApplicationState): Long {
        if (id == null) {
            val newId = applicationDao.insert(
                ApplicationEntity(
                    companyName = state.company,
                    role = state.role,
                    status = state.status.toDbStatus(),
                    dateApplied = state.dateApplied,
                    postingUrl = state.postingUrl,
                    source = state.source,
                    notes = state.notes,
                    nextActionDate = state.nextActionDate,
                ),
            )
            insertStatusHistory(newId, state.statusHistory)
            return newId
        }
        val existingDetails = applicationDao.observeWithDetails(id).first() ?: return id
        applicationDao.update(
            existingDetails.application.copy(
                companyName = state.company,
                role = state.role,
                status = state.status.toDbStatus(),
                dateApplied = state.dateApplied,
                postingUrl = state.postingUrl,
                source = state.source,
                notes = state.notes,
                nextActionDate = state.nextActionDate,
            ),
        )
        // applyEvent only ever appends status-history entries, never edits or removes past ones,
        // so anything past the previously-known count is new and needs inserting.
        insertStatusHistory(id, state.statusHistory.drop(existingDetails.statusHistory.size))
        return id
    }

    private suspend fun insertStatusHistory(applicationId: Long, entries: List<StatusHistoryRecord>) {
        entries.forEach { entry ->
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
