/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.chain

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * An opaque content hash identifying one [EventEnvelope]. This module never computes hashes
 * itself — whoever appends an envelope decides the hash function; the chain-walk algorithms here
 * only ever compare hashes for equality.
 */
@Serializable
data class Hash(val value: String)

/** Identifies the device (chain) an [EventEnvelope] was authored on. */
@Serializable
data class DeviceId(val value: String) {
    companion object {
        fun random(): DeviceId = DeviceId(Uuid.random().toString())
    }
}
