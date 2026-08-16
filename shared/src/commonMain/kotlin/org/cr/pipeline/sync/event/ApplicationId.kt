package org.cr.pipeline.sync.event

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Stable across devices, unlike a Room row id — two devices creating applications independently
 * need identifiers that won't collide once their event logs are synced.
 *
 * A plain wrapper rather than `value class`: Kotlin Multiplatform's cross-target inline value
 * classes are still gated behind an experimental compiler flag at this Kotlin version, and the
 * boxing cost here is irrelevant (event log entries, not a hot path).
 */
@Serializable
data class ApplicationId(val value: String) {
    companion object {
        fun random(): ApplicationId = ApplicationId(Uuid.random().toString())
    }
}
