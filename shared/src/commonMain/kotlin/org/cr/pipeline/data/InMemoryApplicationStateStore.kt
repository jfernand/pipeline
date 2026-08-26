/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.cr.pipeline.model.SeedApplication
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.ContactRecord
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.ReminderRecord
import org.cr.pipeline.sync.event.StatusHistoryRecord
import org.cr.pipeline.sync.event.replayApplicationState

/**
 * The one [ApplicationStateStore] every platform uses — never itself persisted. [eventLog] is the
 * durable source of truth; this materializes it into memory once, by replaying every event
 * through [replayApplicationState] (see there for how ids get assigned and how a bad envelope is
 * tolerated), and keeps that projection live thereafter through ordinary [write]s. Materializing
 * lazily on first access, rather than as an explicit startup step, is what lets both the
 * Compose UI and the MCP server (which never goes through Compose at all) reach a correctly
 * populated store no matter which one touches it first.
 *
 * A fresh install (empty event log) shows [seedApplications] instead — demo content that stays
 * out of the event log entirely, regenerated fresh on every launch, and gone for good — including
 * any edits made to it in the meantime, which become orphan events skipped on the next replay —
 * the moment any real event lands in the log.
 */
class InMemoryApplicationStateStore(
    private val eventLog: EventLog,
    private val logger: Logger = Logger.withTag("InMemoryApplicationStateStore"),
) : ApplicationStateStore {
    private data class Record(val id: Long, val state: ApplicationState)

    private val records = MutableStateFlow<List<Record>>(emptyList())
    private var nextId = 1L
    private val materializeMutex = Mutex()
    private var materialized = false

    // True only while [records] holds seed data rather than a real replay. A create arriving
    // while this is true means the log just went from empty to non-empty — see [write].
    private var showingSeedData = false

    private suspend fun ensureMaterialized() {
        if (materialized) return
        materializeMutex.withLock {
            if (materialized) return@withLock
            val envelopes = eventLog.observeChain().first()
            if (envelopes.isEmpty()) {
                records.value = seedApplications.mapIndexed { index, seed -> seed.toRecord(id = index + 1L) }
                showingSeedData = true
            } else {
                records.value = replayApplicationState(envelopes, todayDate()) { envelope, error ->
                    logger.w(error) { "Skipping unreplayable event: seq=${envelope.sequence} hash=${envelope.hash.value}" }
                }.sortedByDescending { it.first }.map { (id, state) -> Record(id, state) }
                showingSeedData = false
            }
            nextId = (records.value.maxOfOrNull { it.id } ?: 0L) + 1L
            materialized = true
        }
    }

    override fun observeAll(): Flow<List<Pair<Long, ApplicationState>>> = flow {
        ensureMaterialized()
        emitAll(records.map { list -> list.map { it.id to it.state } })
    }

    override fun observeState(id: Long): Flow<ApplicationState?> = flow {
        ensureMaterialized()
        emitAll(records.map { list -> list.find { it.id == id }?.state })
    }

    override suspend fun getState(id: Long): ApplicationState? {
        ensureMaterialized()
        return records.value.find { it.id == id }?.state
    }

    override suspend fun write(id: Long?, state: ApplicationState): Long {
        ensureMaterialized()
        if (id == null) {
            if (showingSeedData) {
                // The first real event just landed — the log this store was materialized from is
                // no longer the empty one that put it in seed mode. Clear seed data so it never
                // coexists with real data, and so real numbering always starts at 1 regardless of
                // whether seed happened to be showing a moment ago (matching what a fresh replay
                // of this same log — e.g. after a restart — would independently produce).
                records.value = emptyList()
                nextId = 1L
                showingSeedData = false
            }
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
            applicationId = ApplicationId.random(),
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
