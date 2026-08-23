/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import androidx.compose.ui.graphics.Color

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
)
