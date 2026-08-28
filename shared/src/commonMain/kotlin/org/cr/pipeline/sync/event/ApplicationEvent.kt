/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.serialization.Serializable
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.AttachmentKind
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
 *  existing list, never a diff against it. [contactId] is assigned here, at construction, rather
 *  than derived from list position — that's what would let a future edit/remove event target this
 *  contact specifically, the way [StatusChanged] targets an application by [applicationId] rather
 *  than "whichever one is current". */
@Serializable
data class ContactAdded(
    override val applicationId: ApplicationId,
    val contactId: ContactId,
    val contact: ContactInput,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent

/** PL-031: attaches a résumé, cover letter, or misc file — the bytes themselves go straight to
 *  [org.cr.pipeline.data.io.FileArchiveService], never into this event's payload; this just
 *  records that it happened and what it was. [attachmentId] is assigned at construction, same
 *  reasoning as [ContactAdded]'s [ContactAdded.contactId]. */
@Serializable
data class AttachmentAdded(
    override val applicationId: ApplicationId,
    val attachmentId: AttachmentId,
    val kind: AttachmentKind,
    val fileName: String,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent

/** Detaches one attachment — removal from the archive, not deletion of the application. Deleting
 *  an application leaves its attachments in the archive alone (see PL-031). */
@Serializable
data class AttachmentRemoved(
    override val applicationId: ApplicationId,
    val attachmentId: AttachmentId,
    override val provenance: EventProvenance = EventProvenance.Unknown,
) : ApplicationEvent
