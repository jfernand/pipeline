package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.ApplicationState
import org.cr.pipeline.sync.event.InMemoryEventLog
import org.cr.pipeline.sync.event.StatusChanged
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private class FakeApplicationStateStore : ApplicationStateStore {
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

class EventSourcedJobApplicationRepositoryTest {
    private val input = ApplicationInput(
        company = "Cedar & Byrne",
        role = "Senior Mobile Engineer",
        status = AppStatus.APPLIED,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = null,
        postingUrl = null,
        source = "LinkedIn",
        notes = "",
    )

    @Test
    fun `saveApplication with no id constructs the application and persists it`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())

        val id = repository.saveApplication(null, input)

        val state = store.states.getValue(id)
        assertEquals(input.company, state.company)
        assertEquals(input.status, state.status)
        assertEquals("Application created", state.statusHistory.single().note)
    }

    @Test
    fun `saveApplication with an existing id edits in place through the same reducer`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        repository.saveApplication(id, input.copy(company = "Northwind Labs", status = AppStatus.OFFER))

        val state = store.states.getValue(id)
        assertEquals("Northwind Labs", state.company)
        assertEquals(AppStatus.OFFER, state.status)
        assertEquals(2, state.statusHistory.size)
    }

    @Test
    fun `updateStatus appends a history entry with its note`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        repository.updateStatus(id, AppStatus.INTERVIEW, "Recruiter screen went well")

        val state = store.states.getValue(id)
        assertEquals(AppStatus.INTERVIEW, state.status)
        assertEquals("Recruiter screen went well", state.statusHistory.last().note)
    }

    @Test
    fun `updateStatus on an unknown id is a no-op`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())

        repository.updateStatus(999, AppStatus.OFFER, "shouldn't happen")

        assertEquals(emptyMap(), store.states)
    }

    @Test
    fun `observeApplicationDetail reflects the stored state and is null when nothing is stored`() = runTest {
        val store = FakeApplicationStateStore()
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val id = repository.saveApplication(null, input)

        val detail = repository.observeApplicationDetail(id).first()
        assertEquals(input.company, detail?.company)
        assertNull(repository.observeApplicationDetail(404).first())
    }

    @Test
    fun `every mutation appends a correctly chained envelope to the event log`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(FakeApplicationStateStore(), eventLog)

        val id = repository.saveApplication(null, input)
        repository.updateStatus(id, AppStatus.OFFER, "Verbal offer")

        val chain = eventLog.observeChain().first()
        assertEquals(2, chain.size)
        assertEquals(emptyList(), chain[0].parentHashes)
        assertEquals(listOf(chain[0].hash), chain[1].parentHashes)
        assertIs<ApplicationCreated>(Json.decodeFromString<ApplicationEvent>(chain[0].payload))
        assertIs<StatusChanged>(Json.decodeFromString<ApplicationEvent>(chain[1].payload))
    }
}
