package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.cr.pipeline.sync.chain.DeviceId
import org.cr.pipeline.sync.chain.EventEnvelope
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.toEnvelope

private const val EVENT_LOG_KEY = "pipeline.eventLog"

/** The whole chain lives under one localStorage key, re-serialized on every append. Simple and
 *  fine at this app's scale (a personal job tracker's event volume, not a multi-user log);
 *  IndexedDB would be the move if that stops being true. */
internal class BrowserEventLog(private val identityStore: DeviceIdentityStore) : EventLog {
    private var cachedDeviceId: DeviceId? = null
    private val chain = MutableStateFlow(readChain())

    override suspend fun deviceId(): DeviceId =
        cachedDeviceId ?: identityStore.getOrCreateDeviceId().also { cachedDeviceId = it }

    override fun observeChain(): Flow<List<EventEnvelope>> = chain

    override suspend fun append(event: ApplicationEvent, timestampEpochMillis: Long): EventEnvelope {
        val id = deviceId()
        val parentHashes = listOfNotNull(chain.value.lastOrNull()?.hash)
        val sequence = (chain.value.lastOrNull()?.sequence ?: -1) + 1
        val envelope = toEnvelope(event, parentHashes, id, sequence, timestampEpochMillis)
        val updated = chain.value + envelope
        chain.value = updated
        localStorageSet(EVENT_LOG_KEY, Json.encodeToString(updated))
        return envelope
    }

    // A corrupted or incompatible-schema value (e.g. left over from a future format change)
    // shouldn't crash the app at launch — treat it as an empty chain instead.
    private fun readChain(): List<EventEnvelope> = localStorageGet(EVENT_LOG_KEY)
        ?.let { runCatching { Json.decodeFromString<List<EventEnvelope>>(it) }.getOrNull() }
        ?: emptyList()
}
