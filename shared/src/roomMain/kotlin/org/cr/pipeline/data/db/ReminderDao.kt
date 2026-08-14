package org.cr.pipeline.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE applicationId = :applicationId ORDER BY dueDate")
    fun observeForApplication(applicationId: Long): Flow<List<Reminder>>

    /** Due, not-yet-fired reminders — what WorkManager polls to decide what to notify. */
    @Query("SELECT * FROM reminders WHERE fired = 0 AND dueDate <= :today")
    suspend fun getDueUnfired(today: LocalDate): List<Reminder>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)
}
