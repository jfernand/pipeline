package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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

/**
 * Used on targets without a Room-backed data layer (js/wasmJs). A mutable in-memory store
 * seeded from [seedApplications]; edits last for the process lifetime, not persisted to disk.
 */
class InMemoryJobApplicationRepository : JobApplicationRepository {
    private data class StatusEventRecord(val status: AppStatus, val date: LocalDate, val note: String)
    private data class ContactRecord(val name: String, val role: String, val email: String)
    private data class ReminderRecord(val message: String, val dueDate: LocalDate)
    private data class Record(
        val id: Long,
        val company: String,
        val role: String,
        val status: AppStatus,
        val dateApplied: LocalDate?,
        val nextActionDate: LocalDate?,
        val postingUrl: String?,
        val source: String?,
        val notes: String,
        val statusHistory: List<StatusEventRecord>,
        val contacts: List<ContactRecord>,
        val reminders: List<ReminderRecord>,
    )

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
            records.update { list -> listOf(input.toNewRecord(newId)) + list }
            return newId
        }
        records.update { list ->
            list.map { record ->
                if (record.id != id) {
                    record
                } else {
                    val updated = record.applyInput(input)
                    if (record.status != input.status) {
                        updated.copy(statusHistory = updated.statusHistory + StatusEventRecord(input.status, todayDate(), "Updated via edit"))
                    } else {
                        updated
                    }
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
                    record.copy(
                        status = status,
                        statusHistory = record.statusHistory + StatusEventRecord(status, todayDate(), note),
                    )
                }
            }
        }
    }

    private fun SeedApplication.toRecord(id: Long): Record {
        val today = todayDate()
        return Record(
            id = id,
            company = company,
            role = role,
            status = status,
            dateApplied = today.minus(daysAgoApplied, DateTimeUnit.DAY),
            nextActionDate = nextActionOffsetDays?.let { today.plus(it, DateTimeUnit.DAY) },
            postingUrl = postingUrl,
            source = source,
            notes = notes,
            statusHistory = statusHistory.map { StatusEventRecord(it.status, today.minus(it.daysAgo, DateTimeUnit.DAY), it.note) },
            contacts = contacts.map { ContactRecord(it.name, it.role, it.email) },
            reminders = reminders.map { ReminderRecord(it.message, today.plus(it.offsetDays, DateTimeUnit.DAY)) },
        )
    }

    private fun ApplicationInput.toNewRecord(id: Long): Record = Record(
        id = id,
        company = company,
        role = role,
        status = status,
        dateApplied = dateApplied,
        nextActionDate = nextActionDate,
        postingUrl = postingUrl,
        source = source,
        notes = notes,
        statusHistory = listOf(StatusEventRecord(status, dateApplied ?: todayDate(), "Application created")),
        contacts = emptyList(),
        reminders = emptyList(),
    )

    private fun Record.applyInput(input: ApplicationInput): Record = copy(
        company = input.company,
        role = input.role,
        status = input.status,
        dateApplied = input.dateApplied,
        nextActionDate = input.nextActionDate,
        postingUrl = input.postingUrl,
        source = input.source,
        notes = input.notes,
    )

    private fun Record.toJobApplication(): JobApplication {
        val today = todayDate()
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

    private fun Record.toApplicationDetail(): ApplicationDetail {
        val today = todayDate()
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

    private fun Record.toApplicationInput(): ApplicationInput = ApplicationInput(
        company = company,
        role = role,
        status = status,
        dateApplied = dateApplied,
        nextActionDate = nextActionDate,
        postingUrl = postingUrl,
        source = source,
        notes = notes,
    )
}
