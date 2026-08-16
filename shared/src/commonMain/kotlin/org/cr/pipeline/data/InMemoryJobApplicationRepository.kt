package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.ReminderSummary
import org.cr.pipeline.model.SeedApplication
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

/**
 * Used on targets without a Room-backed data layer (js/wasmJs). A mutable in-memory store
 * seeded from [seedApplications]; edits last for the process lifetime, not persisted to disk.
 */
class InMemoryJobApplicationRepository : JobApplicationRepository {
    private data class Record(val id: Long, val state: ApplicationState)

    private val records = MutableStateFlow(seedApplications.mapIndexed { index, seed -> seed.toRecord(id = index + 1L) })
    private var nextId = (records.value.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeApplications(): Flow<List<JobApplication>> =
        records.map { list -> list.map { it.toJobApplication() } }

    override fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?> =
        records.map { list -> list.find { it.id == id }?.toApplicationDetail() }

    override suspend fun getApplicationInput(id: Long): ApplicationInput? =
        records.value.find { it.id == id }?.toApplicationInput()

    override suspend fun saveApplication(id: Long?, input: ApplicationInput): Long {
        if (id == null) {
            val newId = nextId++
            // Row ids aren't stable across devices; this stands in until real ids are wired up.
            val state = applyEvent(null, ApplicationCreated(ApplicationId(newId.toString()), input), todayDate())
            records.update { list -> listOf(Record(newId, state)) + list }
            return newId
        }
        records.update { list ->
            list.map { record ->
                if (record.id != id) {
                    record
                } else {
                    record.copy(state = applyEvent(record.state, ApplicationEdited(ApplicationId(id.toString()), input), todayDate()))
                }
            }
        }
        return id
    }

    override suspend fun updateStatus(id: Long, status: AppStatus, note: String) {
        records.update { list ->
            list.map { record ->
                if (record.id != id) {
                    record
                } else {
                    record.copy(state = applyEvent(record.state, StatusChanged(ApplicationId(id.toString()), status, note), todayDate()))
                }
            }
        }
    }

    private fun SeedApplication.toRecord(id: Long): Record {
        val today = todayDate()
        val state = ApplicationState(
            company = company,
            role = role,
            status = status,
            dateApplied = today.minus(daysAgoApplied, DateTimeUnit.DAY),
            nextActionDate = nextActionOffsetDays?.let { today.plus(it, DateTimeUnit.DAY) },
            postingUrl = postingUrl,
            source = source,
            notes = notes,
            statusHistory = statusHistory.map { StatusHistoryRecord(it.status, today.minus(it.daysAgo, DateTimeUnit.DAY), it.note) },
            contacts = contacts.map { ContactRecord(it.name, it.role, it.email) },
            reminders = reminders.map { ReminderRecord(it.message, today.plus(it.offsetDays, DateTimeUnit.DAY)) },
        )
        return Record(id, state)
    }

    private fun Record.toJobApplication(): JobApplication {
        val today = todayDate()
        val daysAgo = state.dateApplied?.let { today.toEpochDays() - it.toEpochDays() } ?: 0
        val overdueDays = state.nextActionDate?.takeIf { it < today }?.let { today.toEpochDays() - it.toEpochDays() }
        val meta = state.dateApplied?.let { "Applied ${it.formatShort()}" } ?: "Saved"
        return JobApplication(
            id = id,
            company = state.company,
            role = state.role,
            status = state.status,
            daysAgo = daysAgo.toInt(),
            meta = meta,
            overdueDays = overdueDays?.toInt(),
        )
    }

    private fun Record.toApplicationDetail(): ApplicationDetail {
        val today = todayDate()
        val orderedHistory = state.statusHistory.sortedBy { it.date }
        return ApplicationDetail(
            id = id,
            company = state.company,
            role = state.role,
            status = state.status,
            daysSinceActivity = orderedHistory.lastOrNull()?.date
                ?.let { (today.toEpochDays() - it.toEpochDays()).toInt() }
                ?: 0,
            source = state.source,
            dateApplied = state.dateApplied?.formatShort(),
            postingUrl = state.postingUrl,
            notes = state.notes,
            statusHistory = orderedHistory.mapIndexed { index, event ->
                StatusHistoryEntry(
                    status = event.status,
                    date = event.date.formatShort(),
                    note = event.note,
                    current = index == orderedHistory.lastIndex,
                )
            },
            contacts = state.contacts.map { ContactSummary(name = it.name, role = it.role, email = it.email) },
            reminders = state.reminders
                .sortedBy { it.dueDate }
                .map { reminder -> ReminderSummary(reminder.message, reminder.dueDate.formatShort(), reminder.dueDate < today) },
        )
    }

    private fun Record.toApplicationInput(): ApplicationInput = ApplicationInput(
        company = state.company,
        role = state.role,
        status = state.status,
        dateApplied = state.dateApplied,
        nextActionDate = state.nextActionDate,
        postingUrl = state.postingUrl,
        source = state.source,
        notes = state.notes,
    )
}
