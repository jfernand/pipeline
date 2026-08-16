package org.cr.pipeline.data

import org.cr.pipeline.sync.chain.DeviceId

private const val DEVICE_ID_KEY = "pipeline.deviceId"

internal class BrowserDeviceIdentityStore : DeviceIdentityStore {
    override suspend fun getOrCreateDeviceId(): DeviceId {
        localStorageGet(DEVICE_ID_KEY)?.let { return DeviceId(it) }
        val newId = DeviceId.random()
        localStorageSet(DEVICE_ID_KEY, newId.value)
        return newId
    }
}
