/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication

interface JobApplicationRepository {
    fun observeApplications(): Flow<List<JobApplication>>
    fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?>

    /** Null when [id] doesn't exist (e.g. it was deleted while the edit screen was open). */
    suspend fun getApplicationInput(id: Long): ApplicationInput?

    /** Creates a new application when [id] is null, otherwise updates the existing one. Returns its id. */
    suspend fun saveApplication(id: Long?, input: ApplicationInput): Long

    /** Records a status change: updates the application's current status and appends a history entry. */
    suspend fun updateStatus(id: Long, status: AppStatus, note: String)
}
