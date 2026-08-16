package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationEdited
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.StatusChanged
import org.cr.pipeline.sync.event.applyEvent
import org.cr.pipeline.sync.event.toApplicationDetail
import org.cr.pipeline.sync.event.toApplicationInput
import org.cr.pipeline.sync.event.toJobApplication

/**
 * The single place that turns a UI-facing mutation into an [org.cr.pipeline.sync.event.ApplicationEvent]
 * and folds it through [applyEvent] — [store] just persists the resulting state. This is what
 * keeps event construction from being duplicated per storage backend the same way field-mapping
 * used to be.
 */
class EventSourcedJobApplicationRepository(private val store: ApplicationStateStore) : JobApplicationRepository {
    override fun observeApplications(): Flow<List<JobApplication>> =
        store.observeAll().map { list -> list.map { (id, state) -> state.toJobApplication(id) } }

    override fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?> =
        store.observeState(id).map { it?.toApplicationDetail(id) }

    override suspend fun getApplicationInput(id: Long): ApplicationInput? =
        store.getState(id)?.toApplicationInput()

    override suspend fun saveApplication(id: Long?, input: ApplicationInput): Long {
        val current = id?.let { store.getState(it) }
        // Row ids aren't stable across devices; for a create, there isn't one yet to key off of
        // anyway, so this is a random placeholder either way until real ids are wired up.
        val event = if (id == null) {
            ApplicationCreated(ApplicationId.random(), input)
        } else {
            ApplicationEdited(ApplicationId(id.toString()), input)
        }
        return store.write(id, applyEvent(current, event, todayDate()))
    }

    override suspend fun updateStatus(id: Long, status: AppStatus, note: String) {
        val current = store.getState(id) ?: return
        val event = StatusChanged(ApplicationId(id.toString()), status, note)
        store.write(id, applyEvent(current, event, todayDate()))
    }
}
