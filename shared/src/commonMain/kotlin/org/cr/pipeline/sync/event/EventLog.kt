/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.EventEnvelope

/**
 * Owns one device's append-only chain: tracks its current tip and next sequence number, and
 * hashes and persists every event appended to it via [toEnvelope]. This is where real
 * [EventEnvelope]s start existing — [org.cr.pipeline.data.EventSourcedJobApplicationRepository]
 * appends to it alongside folding events through [applyEvent]; nothing here knows or cares that
 * the payload happens to be an [ApplicationEvent].
 *
 * [deviceId] is suspend because resolving it may mean reading (or, on first run, writing) durable
 * storage — see [org.cr.pipeline.data.DeviceIdentityStore].
 */
interface EventLog {
    suspend fun deviceId(): DeviceId
    fun observeChain(): Flow<List<EventEnvelope>>
    suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope
}

/**
 * Which of a device's two independent chains a durable [EventLog] implementation reads and
 * writes — [REAL] is the user's actual data; [DEMO] is PL-019's sandbox, seeded once from
 * `seedApplications` (see [org.cr.pipeline.data.DemoSeedingEventLog]) and otherwise a completely
 * ordinary chain from then on. Two chains, not two databases/files, so both platforms' durable
 * [EventLog]s (Room, browser storage) only need one extra key/column to keep them apart, not a
 * second storage implementation.
 */
enum class EventLogKind { REAL, DEMO }

/** Chain lives in memory only — for tests, and as a default before a durable [EventLog] is wired
 *  up for a given platform. Every instance starts a fresh chain under a fresh device id. */
class InMemoryEventLog(private val fixedDeviceId: DeviceId = DeviceId.random()) : EventLog {
    private val chain = MutableStateFlow<List<EventEnvelope>>(emptyList())
    private var nextSequence = 0L

    override suspend fun deviceId(): DeviceId = fixedDeviceId

    override fun observeChain(): Flow<List<EventEnvelope>> = chain

    override suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope {
        val parentHashes = listOfNotNull(chain.value.lastOrNull()?.hash)
        val envelope = toEnvelope(event, parentHashes, fixedDeviceId, nextSequence++, timestampEpochMillis)
        chain.value += envelope
        return envelope
    }
}
