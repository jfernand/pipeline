package org.cr.pipeline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationDetail
import org.koin.compose.koinInject

/** Observes [applicationId]'s detail from the repository, or null while [applicationId] is
 * itself null (e.g. nothing selected yet) — the same loading logic every detail view needs. */
@Composable
fun rememberApplicationDetail(applicationId: Long?): ApplicationDetail? {
    val repository = koinInject<JobApplicationRepository>()
    val detail by produceState<ApplicationDetail?>(initialValue = null, applicationId) {
        if (applicationId == null) {
            value = null
        } else {
            repository.observeApplicationDetail(applicationId).collect { value = it }
        }
    }
    return detail
}
