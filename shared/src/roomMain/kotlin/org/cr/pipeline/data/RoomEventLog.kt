/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cr.pipeline.data.db.EventEnvelopeDao
import org.cr.pipeline.data.db.EventEnvelopeEntity
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.EventEnvelope
import org.cr.pipeline.sync.chain.Hash
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.toEnvelope

internal class RoomEventLog(
    private val identityStore: DeviceIdentityStore,
    private val dao: EventEnvelopeDao,
) : EventLog {
    // Resolved once and cached: identityStore.getOrCreateDeviceId() is a DB round trip, and the
    // device id can't change mid-process.
    private var cachedDeviceId: DeviceId? = null

    override suspend fun deviceId(): DeviceId =
        cachedDeviceId ?: identityStore.getOrCreateDeviceId().also { cachedDeviceId = it }

    override fun observeChain(): Flow<List<EventEnvelope>> =
        dao.observeAll().map { list -> list.map { it.toEventEnvelope() } }

    override suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope {
        val id = deviceId()
        val last = dao.getLast()
        val parentHashes = listOfNotNull(last?.hash?.let { Hash(it) })
        val sequence = (last?.sequence ?: -1) + 1
        val envelope = toEnvelope(event, parentHashes, id, sequence, timestampEpochMillis)
        dao.insert(envelope.toEntity())
        return envelope
    }
}

private fun EventEnvelopeEntity.toEventEnvelope(): EventEnvelope = EventEnvelope(
    hash = Hash(hash),
    parentHashes = parentHashes.map { Hash(it) },
    deviceId = DeviceId(deviceId),
    sequence = sequence,
    timestampEpochMillis = timestampEpochMillis,
    payload = payload,
)

private fun EventEnvelope.toEntity(): EventEnvelopeEntity = EventEnvelopeEntity(
    hash = hash.value,
    parentHashes = parentHashes.map { it.value },
    deviceId = deviceId.value,
    sequence = sequence,
    timestampEpochMillis = timestampEpochMillis,
    payload = payload,
)
