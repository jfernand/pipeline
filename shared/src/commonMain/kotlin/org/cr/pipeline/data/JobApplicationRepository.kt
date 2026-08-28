/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.ContactInput
import org.cr.pipeline.model.FollowUpItem
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.sync.event.EventProvenance

interface JobApplicationRepository {
    fun observeApplications(): Flow<List<JobApplication>>
    fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?>

    /** Every overdue next-action date and overdue reminder, across every application, sorted by
     *  due date — see [org.cr.pipeline.sync.event.toFollowUpItems] for what counts. */
    fun observeFollowUps(): Flow<List<FollowUpItem>>

    /** Null when [id] doesn't exist (e.g. it was deleted while the edit screen was open). */
    suspend fun getApplicationInput(id: Long): ApplicationInput?

    /** Creates a new application when [id] is null, otherwise updates the existing one. Returns
     *  its id. [provenance] left null resolves to this device's own [EventProvenance.Device] —
     *  callers reached through another path (the MCP server) pass their own. */
    suspend fun saveApplication(id: Long?, input: ApplicationInput, provenance: EventProvenance? = null): Long

    /** Records a status change: updates the application's current status and appends a history
     *  entry. [provenance] left null resolves to this device's own [EventProvenance.Device] —
     *  callers reached through another path (the MCP server) pass their own. */
    suspend fun updateStatus(id: Long, status: AppStatus, note: String, provenance: EventProvenance? = null)

    /** Appends [contact] to the application's contact list. [provenance] left null resolves to
     *  this device's own [EventProvenance.Device] — callers reached through another path (the MCP
     *  server) pass their own. */
    suspend fun addContact(id: Long, contact: ContactInput, provenance: EventProvenance? = null)

    /** Writes [bytes] to the file archive (PL-031) as the application's résumé, replacing
     *  whichever one is already there — one slot, same reasoning as
     *  [org.cr.pipeline.sync.event.ResumeAttached]. [provenance] left null resolves to this
     *  device's own [EventProvenance.Device]. */
    suspend fun attachResume(id: Long, fileName: String, bytes: ByteArray, provenance: EventProvenance? = null)

    /** The cover-letter counterpart to [attachResume] — same one-slot-replaces-the-old semantics. */
    suspend fun attachCoverLetter(id: Long, fileName: String, bytes: ByteArray, provenance: EventProvenance? = null)

    /** Writes [bytes] to the file archive (PL-031) as a miscellaneous attachment — unlike
     *  [attachResume]/[attachCoverLetter], there's no slot limit; every call just appends.
     *  [provenance] left null resolves to this device's own [EventProvenance.Device]. */
    suspend fun attachFile(id: Long, fileName: String, bytes: ByteArray, provenance: EventProvenance? = null)

    /** Removes one attachment from the archive and the application's attachment list.
     *  [attachmentId] is an [org.cr.pipeline.model.AttachmentSummary.id]. [provenance] left null
     *  resolves to this device's own [EventProvenance.Device]. */
    suspend fun removeAttachment(id: Long, attachmentId: String, provenance: EventProvenance? = null)
}
