/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Identifies one [AttachmentRecord] within its application's `attachments` list, and doubles as
 * the archive-entry key [org.cr.pipeline.data.io.FileArchiveService] stores its bytes under.
 * Assigned once, at [AttachmentAdded] construction — same reasoning as [ContactId].
 */
@Serializable
data class AttachmentId(val value: String) {
    companion object {
        fun random(): AttachmentId = AttachmentId(Uuid.random().toString())
    }
}
