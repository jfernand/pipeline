/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import com.russhwolf.settings.Settings
import org.cr.pipeline.sync.chain.DeviceId

/** Resolves this device's stable, persisted [DeviceId] — generating and storing one on first
 *  use if none exists yet. */
interface DeviceIdentityStore {
    suspend fun getDeviceId(): DeviceId
}

/** Every platform backs this with the same mechanism — [createPreferencesSettings], via
 *  [SettingsDeviceIdentityStore] — so the id is generated and persisted the same way everywhere,
 *  rather than each platform inventing its own storage. */
expect fun createDeviceIdentityStore(): DeviceIdentityStore

private const val DEVICE_ID_KEY = "pipeline.deviceId"

internal class SettingsDeviceIdentityStore(private val settings: Settings) : DeviceIdentityStore {
    override suspend fun getDeviceId(): DeviceId {
        settings.getStringOrNull(DEVICE_ID_KEY)?.let { return DeviceId(it) }
        val newId = DeviceId.random()
        settings.putString(DEVICE_ID_KEY, newId.value)
        return newId
    }
}
