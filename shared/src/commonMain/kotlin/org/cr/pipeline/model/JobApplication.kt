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
    val company: String,
    val role: String,
    val status: AppStatus,
    val daysAgo: Int,
    val meta: String,
    val overdueDays: Int? = null,
)

val sampleApplications = listOf(
    JobApplication("Northwind Labs", "Staff Android Engineer", AppStatus.INTERVIEW, 2, "Applied Jun 3", overdueDays = 3),
    JobApplication("Cedar & Byrne", "Senior Mobile Engineer", AppStatus.SCREEN, 5, "Applied Jun 11", overdueDays = 1),
    JobApplication("Meridian Systems", "Senior Engineer, Mobile", AppStatus.OFFER, 1, "Applied May 28"),
    JobApplication("Halcyon Freight", "Android Platform Lead", AppStatus.APPLIED, 8, "Applied Jun 16"),
    JobApplication("Ostrom Analytics", "Android Engineer II", AppStatus.WISHLIST, 12, "Saved Jun 12"),
    JobApplication("Kestrel Robotics", "Mobile Infrastructure", AppStatus.REJECTED, 21, "Applied May 14"),
    JobApplication("Tidewater Health", "Senior Android Engineer", AppStatus.WITHDRAWN, 30, "Applied Apr 30"),
)
