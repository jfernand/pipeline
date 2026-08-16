package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import org.cr.pipeline.sync.event.ApplicationState

/**
 * Dumb, business-logic-free storage for [ApplicationState] — no event construction, no field
 * mapping decisions, just persistence. [EventSourcedJobApplicationRepository] is the only thing
 * that constructs events and folds them through the reducer; implementations of this interface
 * (Room, in-memory) only need to know how to store and retrieve the resulting state.
 *
 * Results are ordered newest-first, matching the applications list.
 */
interface ApplicationStateStore {
    fun observeAll(): Flow<List<Pair<Long, ApplicationState>>>
    fun observeState(id: Long): Flow<ApplicationState?>
    suspend fun getState(id: Long): ApplicationState?

    /** Creates a new row when [id] is null, otherwise overwrites the existing one. Returns the row id. */
    suspend fun write(id: Long?, state: ApplicationState): Long
}
