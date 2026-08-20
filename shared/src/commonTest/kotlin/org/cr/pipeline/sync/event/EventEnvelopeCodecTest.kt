/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.Hash
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EventEnvelopeCodecTest {
    private val event = ApplicationCreated(
        ApplicationId("app-1"),
        ApplicationInput(
            company = "Cedar & Byrne",
            role = "Senior Mobile Engineer",
            status = AppStatus.APPLIED,
            dateApplied = LocalDate(2026, 8, 1),
            nextActionDate = null,
            postingUrl = null,
            source = "LinkedIn",
            notes = "",
        ),
    )
    private val deviceId = DeviceId("device-a")

    @Test
    fun `same inputs always produce the same envelope`() {
        val a = toEnvelope(event, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 1000)
        val b = toEnvelope(event, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 1000)

        assertEquals(a, b)
    }

    @Test
    fun `changing the event changes the hash`() {
        val a = toEnvelope(event, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 1000)
        val differentEvent = ApplicationCreated(event.applicationId, event.input.copy(company = "Northwind Labs"))
        val b = toEnvelope(differentEvent, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 1000)

        assertNotEquals(a.hash, b.hash)
    }

    @Test
    fun `changing the chain position changes the hash even for an identical event`() {
        val base = toEnvelope(event, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 1000)
        val differentParent = toEnvelope(event, listOf(Hash("some-parent")), deviceId, sequence = 0, timestampEpochMillis = 1000)
        val differentDevice = toEnvelope(event, emptyList(), DeviceId("device-b"), sequence = 0, timestampEpochMillis = 1000)
        val differentSequence = toEnvelope(event, emptyList(), deviceId, sequence = 1, timestampEpochMillis = 1000)
        val differentTimestamp = toEnvelope(event, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 2000)

        val hashes = listOf(base, differentParent, differentDevice, differentSequence, differentTimestamp).map { it.hash }
        assertEquals(hashes.toSet().size, hashes.size, "every variation should produce a distinct hash")
    }

    @Test
    fun `the payload round-trips back to an equal event`() {
        val envelope = toEnvelope(event, emptyList(), deviceId, sequence = 0, timestampEpochMillis = 1000)

        assertEquals(event, Json.decodeFromString<ApplicationEvent>(envelope.payload))
    }
}
