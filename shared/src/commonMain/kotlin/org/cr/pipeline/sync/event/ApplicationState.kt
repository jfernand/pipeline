/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.AppStatus

/**
 * The raw, storage-shaped fields of one application — the counterpart to
 * [org.cr.pipeline.model.ApplicationDetail] before that model's read-time formatting (dates as
 * display strings, day counts relative to "today"). Produced by folding an application's events
 * through [applyEvent]; each repository implementation maps this to and from its own storage
 * format (Room tables, an in-memory map, ...).
 */
data class ApplicationState(
    val company: String,
    val role: String,
    val status: AppStatus,
    val dateApplied: LocalDate?,
    val nextActionDate: LocalDate?,
    val postingUrl: String?,
    val source: String?,
    val notes: String,
    val statusHistory: List<StatusHistoryRecord>,
    val contacts: List<ContactRecord>,
    val reminders: List<ReminderRecord>,
)

data class StatusHistoryRecord(val status: AppStatus, val date: LocalDate, val note: String)
data class ContactRecord(val name: String, val role: String, val email: String)
data class ReminderRecord(val message: String, val dueDate: LocalDate)
