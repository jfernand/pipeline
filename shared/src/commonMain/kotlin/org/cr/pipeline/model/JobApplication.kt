/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import androidx.compose.ui.graphics.Color

/** Serialized by name into the event log (`ApplicationInput.status`/`StatusChanged.status` inside
 *  `ApplicationEvent`), which is permanent — append-only, never remove or rename an entry. There's
 *  deliberately no runtime fallback for an unrecognized name the way `EventProvenance.Unknown`
 *  exists for provenance: an old build reading a status name it doesn't know fails to decode that
 *  one event (see `replayApplicationState`), it doesn't degrade to some placeholder value. */
enum class AppStatus(val label: String, val color: Color) {
    WISHLIST("Wishlist", Color(0xFF9C9C9C)),
    APPLIED("Applied", Color(0xFF7FA9CC)),
    SCREEN("Phone Screen", Color(0xFF6FB2A8)),
    INTERVIEW("Interviewing", Color(0xFFD9A83C)),
    OFFER("Offer", Color(0xFF6FBF87)),
    REJECTED("Rejected", Color(0xFFC98276)),
    WITHDRAWN("Withdrawn", Color(0xFF6E6E6E)),
}

data class JobApplication(
    val id: Long,
    val company: String,
    val role: String,
    val status: AppStatus,
    val daysAgo: Int,
    // "Created" or "Applied" (whichever date we're measuring from) paired with a relative "Nd
    // ago" string — always computable, unlike a formatted dateApplied, which may not exist yet.
    val activity: Pair<String, String>,
    val overdueDays: Int? = null,
    val source: String? = null,
    /** Whether this application has any attachments at all — an application-level fact, unlike
     *  provenance (PL-034), which belongs to individual events, not the application as a whole. */
    val hasAttachments: Boolean = false,
)
