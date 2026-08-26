/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.sync.event.InMemoryEventLog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class InMemoryApplicationStateStoreTest {
    private val input = ApplicationInput(
        // Deliberately not a name from seedApplications — a test asserting real data isn't
        // confused with seed data would give a false pass if it were.
        company = "Zenith Testing Co",
        role = "Senior Mobile Engineer",
        status = AppStatus.APPLIED,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = null,
        postingUrl = null,
        source = "LinkedIn",
        notes = "",
    )

    @Test
    fun `an empty event log shows seed data`() = runTest {
        val store = InMemoryApplicationStateStore(InMemoryEventLog())

        val applications = store.observeAll().first()

        assertEquals(seedApplications.size, applications.size)
        assertEquals(seedApplications.first().company, applications.first().second.company)
    }

    @Test
    fun `a second store over the same event log recovers identical state, not seed data`() = runTest {
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(InMemoryApplicationStateStore(eventLog), eventLog)
        val id = repository.saveApplication(null, input)
        repository.updateStatus(id, AppStatus.OFFER, "Verbal offer")

        // A fresh store instance, standing in for "the app restarted" — nothing but the event
        // log carries over between these two stores.
        val rebuilt = InMemoryApplicationStateStore(eventLog)
        val applications = rebuilt.observeAll().first()

        assertEquals(1, applications.size)
        val (rebuiltId, state) = applications.single()
        assertEquals(id, rebuiltId)
        assertEquals(input.company, state.company)
        assertEquals(AppStatus.OFFER, state.status)
        assertEquals(2, state.statusHistory.size)
        assertFalse(seedApplications.any { it.company == state.company }, "real data should replace seed data, not sit alongside it")
    }
}
