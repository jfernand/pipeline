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
}
