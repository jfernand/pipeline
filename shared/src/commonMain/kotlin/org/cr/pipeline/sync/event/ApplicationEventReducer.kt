/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import org.cr.pipeline.model.AttachmentKind

/**
 * Folds one [ApplicationEvent] onto the application's current [state] (null only for that
 * application's very first event). This is the *only* place that decides what each event type
 * means — every repository implementation calls this instead of hand-mapping fields itself, so
 * they can't drift from each other, and applying a remote device's synced events works exactly
 * like applying your own.
 *
 * Appends at most one [StatusHistoryRecord], always as the new last element, when an event
 * changes the application's status — callers that need to know what (if anything) is new can
 * compare `result.statusHistory.size` against the input [state]'s.
 *
 * [today] is a parameter rather than read internally so this stays a pure function — the same
 * event applied to the same state always produces the same result, regardless of when it's
 * replayed. It's only used to date a status-history entry when the event doesn't otherwise pin
 * one down (e.g. the input's dateApplied wasn't given).
 */
fun applyEvent(state: ApplicationState?, event: ApplicationEvent, today: LocalDate): ApplicationState = when (event) {
    is ApplicationCreated -> ApplicationState(
        applicationId = event.applicationId,
        company = event.input.company,
        role = event.input.role,
        status = event.input.status,
        dateApplied = event.input.dateApplied,
        nextActionDate = event.input.nextActionDate,
        postingUrl = event.input.postingUrl,
        source = event.input.source,
        notes = event.input.notes,
        statusHistory = listOf(StatusHistoryRecord(event.input.status, event.input.dateApplied ?: today, "Application created")),
        contacts = emptyList(),
        reminders = emptyList(),
        attachments = emptyList(),
    )

    is ApplicationEdited -> {
        val current = checkNotNull(state) { "ApplicationEdited for ${event.applicationId} with no prior state" }
        current.copy(
            company = event.input.company,
            role = event.input.role,
            status = event.input.status,
            dateApplied = event.input.dateApplied,
            nextActionDate = event.input.nextActionDate,
            postingUrl = event.input.postingUrl,
            source = event.input.source,
            notes = event.input.notes,
            statusHistory = if (current.status != event.input.status) {
                current.statusHistory + StatusHistoryRecord(event.input.status, today, "Updated via edit")
            } else {
                current.statusHistory
            },
        )
    }

    is StatusChanged -> {
        val current = checkNotNull(state) { "StatusChanged for ${event.applicationId} with no prior state" }
        current.copy(
            status = event.status,
            statusHistory = current.statusHistory + StatusHistoryRecord(event.status, today, event.note),
        )
    }

    is ContactAdded -> {
        val current = checkNotNull(state) { "ContactAdded for ${event.applicationId} with no prior state" }
        current.copy(
            contacts = current.contacts + ContactRecord(event.contactId, event.contact.name, event.contact.role, event.contact.email),
        )
    }

    is AttachmentAdded -> {
        val current = checkNotNull(state) { "AttachmentAdded for ${event.applicationId} with no prior state" }
        // RESUME/COVER_LETTER are one slot each — adding a new one replaces whichever one of the
        // same kind is already there; MISC has no slot limit, every add just appends.
        val withoutSameSlot = if (event.kind == AttachmentKind.MISC) {
            current.attachments
        } else {
            current.attachments.filterNot { it.kind == event.kind }
        }
        current.copy(
            attachments = withoutSameSlot + AttachmentRecord(event.attachmentId, event.kind, event.fileName),
        )
    }

    is AttachmentRemoved -> {
        val current = checkNotNull(state) { "AttachmentRemoved for ${event.applicationId} with no prior state" }
        current.copy(attachments = current.attachments.filterNot { it.id == event.attachmentId })
    }
}
