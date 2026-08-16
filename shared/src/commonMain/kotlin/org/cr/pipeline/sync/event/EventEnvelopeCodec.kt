package org.cr.pipeline.sync.event

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.EventEnvelope
import org.cr.pipeline.sync.chain.Hash
import org.cr.pipeline.sync.chain.sha256
import org.cr.pipeline.sync.chain.toHex

private val json = Json

/**
 * Serializes [event] and hashes it together with its chain position into a real
 * [EventEnvelope] — the bridge between this module's domain events and sync-core's
 * domain-agnostic chain primitives. Pure and deterministic: the same arguments always produce
 * the same envelope, which is what lets [org.cr.pipeline.sync.chain.diffChains] treat hashes as
 * reliable identity.
 */
fun toEnvelope(
    event: ApplicationEvent,
    parentHashes: List<Hash>,
    deviceId: DeviceId,
    sequence: Long,
    timestampEpochMillis: Long,
): EventEnvelope {
    val payload = json.encodeToString(event)
    val hashInput = buildString {
        append(deviceId.value)
        append('|')
        append(sequence)
        append('|')
        append(timestampEpochMillis)
        append('|')
        parentHashes.forEach { append(it.value); append(',') }
        append('|')
        append(payload)
    }
    return EventEnvelope(
        hash = Hash(sha256(hashInput.encodeToByteArray()).toHex()),
        parentHashes = parentHashes,
        deviceId = deviceId,
        sequence = sequence,
        timestampEpochMillis = timestampEpochMillis,
        payload = payload,
    )
}
