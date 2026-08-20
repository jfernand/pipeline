/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "applications")
data class Application(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyName: String,
    val role: String,
    val status: ApplicationStatus,
    val dateApplied: LocalDate?,
    val postingUrl: String?,
    val source: String?, // "referral", "LinkedIn", "company site", etc.
    val notes: String = "",
    val nextActionDate: LocalDate? = null,
    val createdAt: Instant = Clock.System.now(),
)
