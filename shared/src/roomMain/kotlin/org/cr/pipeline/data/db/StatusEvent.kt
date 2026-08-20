/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "status_events",
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
data class StatusEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val applicationId: Long,
    val status: ApplicationStatus,
    val date: LocalDate,
    val note: String = "",
)
