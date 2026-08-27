/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.serialization.Serializable
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.ContactInput

/**
 * One user-initiated change to an application, in the shape it needs to be replayed or diffed —
 * independent of how it's hashed, chained, or transported (that's the sync module's job, and it
 * never looks inside these).
 */
@Serializable
sealed interface ApplicationEvent {
    val applicationId: ApplicationId

    /** Defaults to [EventProvenance.Unknown] so a payload written before this field existed —
     *  which has no "provenance" key at all — still decodes, rather than failing to load. */
    val provenance: EventProvenance
}

@Serializable
data class ApplicationCreated(
    override val applicationId: ApplicationId,
    val input: ApplicationInput,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent

/** A full save from the edit form — always the complete field set, not a diff, so it can be
 *  applied without needing to know the application's prior state. */
@Serializable
data class ApplicationEdited(
    override val applicationId: ApplicationId,
    val input: ApplicationInput,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent

/** The dedicated "Update status" sheet, distinct from [ApplicationEdited] because it always
 *  carries a [note] for the status-history entry, whereas an edit only touches status
 *  incidentally when that field happens to differ. */
@Serializable
data class StatusChanged(
    override val applicationId: ApplicationId,
    val status: AppStatus,
    val note: String,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent

/** The "Contacts" section's "+" affordance — appends one contact, always to the end of the
 *  existing list, never a diff against it. */
@Serializable
data class ContactAdded(
    override val applicationId: ApplicationId,
    val contact: ContactInput,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent
