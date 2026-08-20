/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.cr.pipeline.sync.event.ApplicationState

/** Shared in-memory [ApplicationStateStore] fake — used anywhere a test needs a real
 *  [EventSourcedJobApplicationRepository] without a database, including from other source sets
 *  (e.g. jvmTest) that depend on commonTest. */
class FakeApplicationStateStore : ApplicationStateStore {
    val states = mutableMapOf<Long, ApplicationState>()
    private var nextId = 1L

    override fun observeAll(): Flow<List<Pair<Long, ApplicationState>>> = flowOf(states.toList())
    override fun observeState(id: Long): Flow<ApplicationState?> = flowOf(states[id])
    override suspend fun getState(id: Long): ApplicationState? = states[id]

    override suspend fun write(id: Long?, state: ApplicationState): Long {
        val actualId = id ?: nextId++
        states[actualId] = state
        return actualId
    }
}
