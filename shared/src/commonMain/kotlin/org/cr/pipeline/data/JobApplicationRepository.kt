package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import org.cr.pipeline.model.JobApplication

interface JobApplicationRepository {
    fun observeApplications(): Flow<List<JobApplication>>
}
