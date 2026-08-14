package org.cr.pipeline.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Application::class,
            parentColumns = ["id"],
            childColumns = ["applicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("applicationId")],
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val applicationId: Long,
    val dueDate: LocalDate,
    val message: String,
    val fired: Boolean = false, // used by WorkManager to avoid duplicate notifications
)
