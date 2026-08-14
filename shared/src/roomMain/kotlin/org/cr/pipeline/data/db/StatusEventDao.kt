package org.cr.pipeline.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusEventDao {
    @Query("SELECT * FROM status_events WHERE applicationId = :applicationId ORDER BY date DESC")
    fun observeForApplication(applicationId: Long): Flow<List<StatusEvent>>

    @Insert
    suspend fun insert(event: StatusEvent): Long

    @Delete
    suspend fun delete(event: StatusEvent)
}
