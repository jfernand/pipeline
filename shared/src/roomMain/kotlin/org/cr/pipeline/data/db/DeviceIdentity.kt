package org.cr.pipeline.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Single-row table: this device's stable id, generated once and reused across restarts. */
@Entity(tableName = "device_identity")
data class DeviceIdentity(
    @PrimaryKey val id: Int = 0,
    val deviceId: String,
)

@Dao
interface DeviceIdentityDao {
    @Query("SELECT deviceId FROM device_identity WHERE id = 0")
    suspend fun getDeviceId(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDeviceId(entity: DeviceIdentity)
}
