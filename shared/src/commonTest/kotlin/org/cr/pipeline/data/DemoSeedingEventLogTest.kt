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
import org.cr.pipeline.sync.event.ApplicationCreated
import org.cr.pipeline.sync.event.ApplicationId
import org.cr.pipeline.sync.event.InMemoryEventLog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DemoSeedingEventLogTest {
    // One ApplicationCreated per seed entry, plus one ContactAdded per contact in that entry.
    private val expectedEventCount = seedApplications.size + seedApplications.sumOf { it.contacts.size }

    @Test
    fun `an empty demo log is seeded with one ApplicationCreated and one ContactAdded per seed contact`() = runTest {
        val demo = DemoSeedingEventLog(InMemoryEventLog())

        val chain = demo.observeChain().first()

        assertEquals(expectedEventCount, chain.size)
    }

    @Test
    fun `seeding is idempotent — a second read doesn't seed again`() = runTest {
        val demo = DemoSeedingEventLog(InMemoryEventLog())
        demo.observeChain().first()

        val chain = demo.observeChain().first()

        assertEquals(expectedEventCount, chain.size)
    }

    @Test
    fun `a demo log that already has events is left exactly as it is`() = runTest {
        val delegate = InMemoryEventLog()
        val input = ApplicationInput(
            company = "Already Here Inc",
            role = "Engineer",
            status = AppStatus.APPLIED,
            dateApplied = LocalDate(2026, 8, 1),
            nextActionDate = null,
            postingUrl = null,
            source = null,
            notes = "",
        )
        delegate.append(ApplicationCreated(ApplicationId.random(), input), 1000L)
        val demo = DemoSeedingEventLog(delegate)

        val chain = demo.observeChain().first()

        assertEquals(1, chain.size)
    }

    @Test
    fun `seeded events replay into applications matching seedApplications' companies`() = runTest {
        val demo = DemoSeedingEventLog(InMemoryEventLog())
        val store = InMemoryApplicationStateStore(demo)

        val applications = store.observeAll().first()

        assertEquals(seedApplications.map { it.company }.toSet(), applications.map { it.second.company }.toSet())
    }

    @Test
    fun `append triggers seeding just as reading does`() = runTest {
        val delegate = InMemoryEventLog()
        val demo = DemoSeedingEventLog(delegate)
        val input = ApplicationInput(
            company = "Triggers Seeding Inc",
            role = "Engineer",
            status = AppStatus.APPLIED,
            dateApplied = LocalDate(2026, 8, 1),
            nextActionDate = null,
            postingUrl = null,
            source = null,
            notes = "",
        )

        demo.append(ApplicationCreated(ApplicationId.random(), input), 1000L)

        val chain = delegate.observeChain().first()
        assertEquals(expectedEventCount + 1, chain.size)
        assertTrue(chain.any { it.payload.contains("Triggers Seeding Inc") })
    }

    @Test
    fun `seeded contacts replay onto their application`() = runTest {
        val demo = DemoSeedingEventLog(InMemoryEventLog())
        val store = InMemoryApplicationStateStore(demo)

        val applications = store.observeAll().first()

        val northwind = applications.single { it.second.company == "Northwind Labs" }.second
        assertEquals(listOf("Dana Whitfield", "Marcus Oyelaran"), northwind.contacts.map { it.name })

        val halcyon = applications.single { it.second.company == "Halcyon Freight" }.second
        assertEquals(emptyList(), halcyon.contacts)
    }
}
