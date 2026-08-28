/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Identifies one [ContactRecord] within its application's `contacts` list — without this, a
 * future "edit" or "remove" event would have nothing stable to target (an index shifts as
 * contacts are added/removed; name+email can collide or change). Stable across devices for the
 * same reason [ApplicationId] is: assigned once, at [ContactAdded] construction, never derived
 * from position.
 */
@Serializable
data class ContactId(val value: String) {
    companion object {
        fun random(): ContactId = ContactId(Uuid.random().toString())
    }
}
