/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.chain

import kotlinx.serialization.Serializable

/**
 * One node in a device's event chain (or, at a conflict-resolution point, in the small DAG a
 * merge produces). [payload] is opaque here by design — the app's domain event types live in the
 * `shared` module, which this one never depends on — so this module can hash, compare, and walk
 * chains without knowing anything about applications, statuses, or any other domain concept.
 *
 * [parentHashes] is normally a single hash (the previous event on this device); empty only for a
 * device's very first event; two entries mark a merge event reconciling a conflict between two
 * devices' chains.
 */
@Serializable
data class EventEnvelope(
    val hash: Hash,
    val parentHashes: List<Hash>,
    val deviceId: DeviceId,
    val sequence: Long,
    val timestampEpochMillis: Long,
    val payload: String,
)
