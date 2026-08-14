package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.sampleApplications

/** Used on targets without a Room-backed data layer (js/wasmJs). */
class InMemoryJobApplicationRepository : JobApplicationRepository {
    override fun observeApplications(): Flow<List<JobApplication>> = flowOf(sampleApplications)
}
