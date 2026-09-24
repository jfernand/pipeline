/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import org.cr.pipeline.sync.event.EventProvenance

data class ApplicationDetail(
    val id: Long,
    val company: String,
    val role: String,
    val status: AppStatus,
    val daysSinceActivity: Int,
    val source: String?,
    val dateApplied: String?,
    val postingUrl: String?,
    val notes: String,
    val statusHistory: List<StatusHistoryEntry>,
    val contacts: List<ContactSummary>,
    val reminders: List<ReminderSummary>,
    val attachments: List<AttachmentSummary>,
)

/** Oldest first; [current] marks the most recent entry (the application's present status).
 *  PL-034: [provenance] is who or what made *this* entry's change — not a summary of the whole
 *  application, which can span history from both a device and an MCP client. */
data class StatusHistoryEntry(
    val status: AppStatus,
    val date: String,
    val note: String,
    val current: Boolean,
    val provenance: EventProvenance = EventProvenance.Unknown,
)

data class ContactSummary(
    val id: String,
    val name: String,
    val role: String,
    val email: String,
)

data class ReminderSummary(
    val message: String,
    val dueDate: String,
    val overdue: Boolean,
)

data class AttachmentSummary(
    val id: String,
    val kind: AttachmentKind,
    val fileName: String,
)
