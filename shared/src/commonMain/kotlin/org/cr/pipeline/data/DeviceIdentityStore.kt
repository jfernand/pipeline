/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import org.cr.pipeline.sync.chain.DeviceId

/** Resolves this device's stable, persisted [DeviceId] — generating and storing one on first
 *  use if none exists yet. */
interface DeviceIdentityStore {
    suspend fun getOrCreateDeviceId(): DeviceId
}
