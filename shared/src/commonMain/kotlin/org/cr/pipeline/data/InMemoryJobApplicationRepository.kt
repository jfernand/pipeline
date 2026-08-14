package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.seedApplications
import org.cr.pipeline.model.toApplicationDetail
import org.cr.pipeline.model.toJobApplication

/** Used on targets without a Room-backed data layer (js/wasmJs). Reads straight from the seed data. */
class InMemoryJobApplicationRepository : JobApplicationRepository {
    private val applications = seedApplications.mapIndexed { index, seed -> seed.toJobApplication(id = index + 1L) }

    override fun observeApplications(): Flow<List<JobApplication>> = flowOf(applications)

    override fun observeApplicationDetail(id: Long): Flow<ApplicationDetail?> {
        val index = seedApplications.indices.firstOrNull { it + 1L == id }
        return flowOf(index?.let { seedApplications[it].toApplicationDetail(id) })
    }
}
