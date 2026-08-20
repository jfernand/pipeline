/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
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
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val applicationId: Long,
    val name: String,
    val role: String?, // "recruiter", "hiring manager"
    val email: String?,
    val linkedinUrl: String?,
)
