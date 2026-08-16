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
 */
interface EventLog {
    val deviceId: DeviceId
    fun observeChain(): Flow<List<EventEnvelope>>
    suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope
}

/** Chain lives in memory only — durable storage (Room, ...) is future work; every process start
 *  begins a fresh chain under a fresh [deviceId]. */
class InMemoryEventLog(override val deviceId: DeviceId = DeviceId.random()) : EventLog {
    private val chain = MutableStateFlow<List<EventEnvelope>>(emptyList())
    private var nextSequence = 0L

    override fun observeChain(): Flow<List<EventEnvelope>> = chain

    override suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope {
        val parentHashes = listOfNotNull(chain.value.lastOrNull()?.hash)
        val envelope = toEnvelope(event, parentHashes, deviceId, nextSequence++, timestampEpochMillis)
        chain.value += envelope
        return envelope
    }
}
