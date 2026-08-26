/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.EventEnvelope
import org.cr.pipeline.sync.chain.Hash
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplayApplicationStateTest {
    private val today = LocalDate(2026, 8, 25)
    private val deviceId = DeviceId("device-a")

    private val inputA = ApplicationInput(
        company = "Cedar & Byrne",
        role = "Senior Mobile Engineer",
        status = AppStatus.APPLIED,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = null,
        postingUrl = null,
        source = "LinkedIn",
        notes = "",
    )
    private val inputB = inputA.copy(company = "Northwind Labs", role = "Staff Engineer")

    // Sequence and parent hashes don't matter to replay itself (only decode order does), so every
    // envelope here just chains onto the previous one for realism, not because replay checks it.
    private fun buildEnvelopes(events: List<ApplicationEvent>): List<EventEnvelope> {
        var parent: Hash? = null
        return events.mapIndexed { index, event ->
            val payload = Json.encodeToString(event)
            val envelope = EventEnvelope(
                hash = Hash("hash-$index"),
                parentHashes = listOfNotNull(parent),
                deviceId = deviceId,
                sequence = index.toLong(),
                timestampEpochMillis = 1000L + index,
                payload = payload,
            )
            parent = envelope.hash
            envelope
        }
    }

    @Test
    fun `replay of one application's full history equals folding the same events incrementally`() {
        val appId = ApplicationId("app-1")
        val events = listOf(
            ApplicationCreated(appId, inputA),
            ApplicationEdited(appId, inputA.copy(company = "Cedar & Byrne (renamed)")),
            StatusChanged(appId, AppStatus.OFFER, "Verbal offer"),
        )
        val expected = events.fold(null as ApplicationState?) { state, event -> applyEvent(state, event, today) }

        val replayed = replayApplicationState(buildEnvelopes(events), today)

        assertEquals(listOf(1L to expected), replayed)
    }

    @Test
    fun `two interleaved applications each get correct, independent state and distinct ids`() {
        val appA = ApplicationId("app-a")
        val appB = ApplicationId("app-b")
        val events = listOf(
            ApplicationCreated(appA, inputA),
            ApplicationCreated(appB, inputB),
            ApplicationEdited(appA, inputA.copy(status = AppStatus.OFFER)),
        )

        val replayed = replayApplicationState(buildEnvelopes(events), today)

        assertEquals(2, replayed.size)
        val (idA, stateA) = replayed[0]
        val (idB, stateB) = replayed[1]
        assertEquals(1L, idA)
        assertEquals(2L, idB)
        assertEquals("Cedar & Byrne", stateA.company)
        assertEquals(AppStatus.OFFER, stateA.status)
        assertEquals("Northwind Labs", stateB.company)
    }

    @Test
    fun `id assignment is stable across two replays of the identical envelope list`() {
        val events = listOf(ApplicationCreated(ApplicationId("app-1"), inputA), ApplicationCreated(ApplicationId("app-2"), inputB))
        val envelopes = buildEnvelopes(events)

        val first = replayApplicationState(envelopes, today).map { it.first }
        val second = replayApplicationState(envelopes, today).map { it.first }

        assertEquals(first, second)
        assertEquals(listOf(1L, 2L), first)
    }

    @Test
    fun `a malformed envelope is skipped without affecting other applications`() {
        val goodEvent = ApplicationCreated(ApplicationId("app-1"), inputA)
        val envelopes = buildEnvelopes(listOf(goodEvent)) +
            EventEnvelope(Hash("bad"), emptyList(), deviceId, sequence = 1, timestampEpochMillis = 2000, payload = "not json")
        val skipped = mutableListOf<EventEnvelope>()

        val replayed = replayApplicationState(envelopes, today, onSkippedEnvelope = { envelope, _ -> skipped.add(envelope) })

        assertEquals(1, replayed.size)
        assertEquals("Cedar & Byrne", replayed.single().second.company)
        assertEquals(1, skipped.size)
        assertEquals("bad", skipped.single().hash.value)
    }

    @Test
    fun `an edit or status change with no prior create is skipped, not thrown`() {
        val orphanId = ApplicationId("orphan")
        val events = listOf(ApplicationEdited(orphanId, inputA), StatusChanged(orphanId, AppStatus.OFFER, "note"))
        val skipped = mutableListOf<EventEnvelope>()

        val replayed = replayApplicationState(buildEnvelopes(events), today, onSkippedEnvelope = { envelope, _ -> skipped.add(envelope) })

        assertTrue(replayed.isEmpty())
        assertEquals(2, skipped.size)
    }

    @Test
    fun `an empty log replays to an empty result`() {
        assertEquals(emptyList(), replayApplicationState(emptyList(), today))
    }
}
