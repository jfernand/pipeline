package org.cr.pipeline.data

import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.model.sampleApplications

/** Backed by static sample data for now; swap the body out once there's a real data source. */
class JobApplicationRepository {
    fun getApplications(): List<JobApplication> = sampleApplications
}
