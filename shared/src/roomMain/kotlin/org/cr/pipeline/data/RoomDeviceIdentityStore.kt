package org.cr.pipeline.data

import org.cr.pipeline.data.db.DeviceIdentity
import org.cr.pipeline.data.db.DeviceIdentityDao
import org.cr.pipeline.sync.chain.DeviceId

internal class RoomDeviceIdentityStore(private val dao: DeviceIdentityDao) : DeviceIdentityStore {
    override suspend fun getOrCreateDeviceId(): DeviceId {
        dao.getDeviceId()?.let { return DeviceId(it) }
        val newId = DeviceId.random()
        dao.setDeviceId(DeviceIdentity(deviceId = newId.value))
        return newId
    }
}
