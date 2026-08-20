/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** An application together with its status history, contacts, and reminders. */
data class ApplicationWithDetails(
    @Embedded val application: Application,
    @Relation(parentColumn = "id", entityColumn = "applicationId")
    val statusHistory: List<StatusEvent>,
    @Relation(parentColumn = "id", entityColumn = "applicationId")
    val contacts: List<Contact>,
    @Relation(parentColumn = "id", entityColumn = "applicationId")
    val reminders: List<Reminder>,
)

@Dao
interface ApplicationDao {
    @Transaction
    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun observeAllWithDetails(): Flow<List<ApplicationWithDetails>>

    @Transaction
    @Query("SELECT * FROM applications WHERE id = :id")
    fun observeWithDetails(id: Long): Flow<ApplicationWithDetails?>

    @Query("SELECT COUNT(*) FROM applications")
    suspend fun count(): Int

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getById(id: Long): Application?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: Application): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(applications: List<Application>)

    @Update
    suspend fun update(application: Application)

    @Delete
    suspend fun delete(application: Application)
}
