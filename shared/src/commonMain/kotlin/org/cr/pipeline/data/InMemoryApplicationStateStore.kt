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
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.EventLog
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
 * A fresh install (empty event log) is genuinely empty — no synthesized demo content here, not
 * since PL-019. [eventLog] is the only thing this store ever reads from; if [eventLog] happens to
 * be a [org.cr.pipeline.data.DemoSeedingEventLog], that's an entirely separate concern this class
 * doesn't know or care about.
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

    private suspend fun ensureMaterialized() {
        if (materialized) return
        materializeMutex.withLock {
            if (materialized) return@withLock
            val envelopes = eventLog.observeChain().first()
            records.value = replayApplicationState(envelopes, todayDate()) { envelope, error ->
                logger.w(error) { "Skipping unreplayable event: seq=${envelope.sequence} hash=${envelope.hash.value}" }
            }.sortedByDescending { it.first }.map { (id, state) -> Record(id, state) }
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
            val newId = nextId++
            records.update { list -> listOf(Record(newId, state)) + list }
            return newId
        }
        records.update { list -> list.map { record -> if (record.id == id) record.copy(state = state) else record } }
        return id
    }
}
