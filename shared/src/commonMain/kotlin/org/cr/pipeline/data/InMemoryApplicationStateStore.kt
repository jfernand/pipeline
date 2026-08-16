package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.cr.pipeline.model.SeedApplication
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.ContactRecord
import org.cr.pipeline.sync.event.ReminderRecord
import org.cr.pipeline.sync.event.StatusHistoryRecord

/**
 * Used on targets without a Room-backed data layer (js/wasmJs). A mutable in-memory store
 * seeded from [seedApplications]; edits last for the process lifetime, not persisted to disk.
 */
class InMemoryApplicationStateStore : ApplicationStateStore {
    private data class Record(val id: Long, val state: ApplicationState)

    private val records = MutableStateFlow(seedApplications.mapIndexed { index, seed -> seed.toRecord(id = index + 1L) })
    private var nextId = (records.value.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeAll(): Flow<List<Pair<Long, ApplicationState>>> =
        records.map { list -> list.map { it.id to it.state } }

    override fun observeState(id: Long): Flow<ApplicationState?> =
        records.map { list -> list.find { it.id == id }?.state }

    override suspend fun getState(id: Long): ApplicationState? =
        records.value.find { it.id == id }?.state

    override suspend fun write(id: Long?, state: ApplicationState): Long {
        if (id == null) {
            val newId = nextId++
            records.update { list -> listOf(Record(newId, state)) + list }
            return newId
        }
        records.update { list -> list.map { record -> if (record.id == id) record.copy(state = state) else record } }
        return id
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
}
