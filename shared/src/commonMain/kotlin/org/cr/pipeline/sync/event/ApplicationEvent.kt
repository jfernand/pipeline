/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.serialization.Serializable
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus

/**
 * One user-initiated change to an application, in the shape it needs to be replayed or diffed —
 * independent of how it's hashed, chained, or transported (that's the sync module's job, and it
 * never looks inside these).
 */
@Serializable
sealed interface ApplicationEvent {
    val applicationId: ApplicationId
}

@Serializable
data class ApplicationCreated(
    override val applicationId: ApplicationId,
    val input: ApplicationInput,
) : ApplicationEvent

/** A full save from the edit form — always the complete field set, not a diff, so it can be
 *  applied without needing to know the application's prior state. */
@Serializable
data class ApplicationEdited(
    override val applicationId: ApplicationId,
    val input: ApplicationInput,
) : ApplicationEvent

/** The dedicated "Update status" sheet, distinct from [ApplicationEdited] because it always
 *  carries a [note] for the status-history entry, whereas an edit only touches status
 *  incidentally when that field happens to differ. */
@Serializable
data class StatusChanged(
    override val applicationId: ApplicationId,
    val status: AppStatus,
    val note: String,
) : ApplicationEvent
