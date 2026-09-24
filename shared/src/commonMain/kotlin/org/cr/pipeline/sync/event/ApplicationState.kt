/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.AttachmentKind

/**
 * The raw, storage-shaped fields of one application — the counterpart to
 * [org.cr.pipeline.model.ApplicationDetail] before that model's read-time formatting (dates as
 * display strings, day counts relative to "today"). Produced by folding an application's events
 * through [applyEvent]; each repository implementation maps this to and from its own storage
 * format (Room tables, an in-memory map, ...).
 */
data class ApplicationState(
    val applicationId: ApplicationId,
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
    val attachments: List<AttachmentRecord>,
    /** PL-024: a soft delete — [ApplicationDeleted] sets this, nothing ever removes an
     *  application's state or history outright. Every read path filters it out. */
    val deleted: Boolean = false,
)

/** PL-034: [provenance] is who or what made *this* change — not a summary of the whole
 *  application, which can span history from both a device and an MCP client. Set from the
 *  originating event's own provenance in [applyEvent]. */
data class StatusHistoryRecord(
    val status: AppStatus,
    val date: LocalDate,
    val note: String,
    val provenance: EventProvenance = EventProvenance.Unknown,
)
data class ContactRecord(val id: ContactId, val name: String, val role: String, val email: String)
data class ReminderRecord(val message: String, val dueDate: LocalDate)

/** No bytes — those live only in [org.cr.pipeline.data.io.FileArchiveService]'s archive, keyed by
 *  [id]. This is just the metadata a real event can carry. */
data class AttachmentRecord(val id: AttachmentId, val kind: AttachmentKind, val fileName: String)
