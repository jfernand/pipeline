/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class SeedStatusEvent(val status: AppStatus, val daysAgo: Int, val note: String)
data class SeedContact(val name: String, val role: String, val email: String)

/** [offsetDays] relative to today: negative is overdue, positive is upcoming. */
data class SeedReminder(val offsetDays: Int, val message: String)

data class SeedApplication(
    val company: String,
    val role: String,
    val status: AppStatus,
    val daysAgoApplied: Int,
    val nextActionOffsetDays: Int?,
    val source: String?,
    val postingUrl: String?,
    val notes: String,
    val statusHistory: List<SeedStatusEvent>,
    val contacts: List<SeedContact>,
    val reminders: List<SeedReminder>,
)

/**
 * The one source of test/QA data for the app: [org.cr.pipeline.data.RoomJobApplicationRepository]
 * seeds the database from this on first run, and [org.cr.pipeline.data.InMemoryJobApplicationRepository]
 * (js/wasmJs, no Room) reads it directly. Dates are relative offsets from "today" so the app always
 * looks current whenever it's actually run.
 */
val seedApplications: List<SeedApplication> = listOf(
    SeedApplication(
        company = "Northwind Labs",
        role = "Staff Android Engineer",
        status = AppStatus.INTERVIEW,
        daysAgoApplied = 11,
        nextActionOffsetDays = -3,
        source = "Referral",
        postingUrl = "northwindlabs.com/careers/staff-android",
        notes = "Round 2 is systems design. They run Compose across the whole app and asked how we handle " +
            "multi-module builds. Comp band 185–205 plus equity. Dana said a decision lands within two " +
            "weeks of the final round.\n\nPrep: draw the sync topology from memory. They asked twice about " +
            "offline conflict handling, so it matters to them.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.APPLIED, daysAgo = 11, note = "Referred by Dana W."),
            SeedStatusEvent(AppStatus.SCREEN, daysAgo = 4, note = "30 min with recruiter"),
            SeedStatusEvent(AppStatus.INTERVIEW, daysAgo = 2, note = "Round 1: Compose deep dive"),
        ),
        contacts = listOf(
            SeedContact("Dana Whitfield", "Engineering manager", "dana@northwindlabs.com"),
            SeedContact("Marcus Oyelaran", "Recruiter", "marcus@northwindlabs.com"),
        ),
        reminders = listOf(
            SeedReminder(-3, "Email Dana about round 2 timing"),
            SeedReminder(6, "Systems design round"),
            SeedReminder(11, "Nudge if no reply"),
        ),
    ),
    SeedApplication(
        company = "Cedar & Byrne",
        role = "Senior Mobile Engineer",
        status = AppStatus.SCREEN,
        daysAgoApplied = 5,
        nextActionOffsetDays = -1,
        source = "LinkedIn",
        postingUrl = "cedarandbyrne.com/careers/senior-mobile",
        notes = "Team is expanding the mobile platform group after the last product launch. Recruiter " +
            "mentioned a fast timeline — they want to fill the role within a month.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.APPLIED, daysAgo = 5, note = "Applied via LinkedIn Easy Apply"),
            SeedStatusEvent(AppStatus.SCREEN, daysAgo = 1, note = "Recruiter screen scheduled"),
        ),
        contacts = listOf(
            SeedContact("Priya Nathan", "Recruiter", "priya.nathan@cedarandbyrne.com"),
        ),
        reminders = listOf(
            SeedReminder(-1, "Confirm screen call time"),
        ),
    ),
    SeedApplication(
        company = "Meridian Systems",
        role = "Senior Engineer, Mobile",
        status = AppStatus.OFFER,
        daysAgoApplied = 27,
        nextActionOffsetDays = 3,
        source = "Company site",
        postingUrl = "meridiansystems.io/careers/senior-mobile",
        notes = "Verbal offer from the hiring manager: base + equity, written offer to follow. Team seems " +
            "strong, good rapport in the final round.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.APPLIED, daysAgo = 27, note = "Applied through company site"),
            SeedStatusEvent(AppStatus.SCREEN, daysAgo = 20, note = "Recruiter screen"),
            SeedStatusEvent(AppStatus.INTERVIEW, daysAgo = 8, note = "Onsite loop, 4 rounds"),
            SeedStatusEvent(AppStatus.OFFER, daysAgo = 1, note = "Verbal offer from hiring manager"),
        ),
        contacts = listOf(
            SeedContact("Owen Castellano", "Hiring manager", "owen.castellano@meridiansystems.io"),
        ),
        reminders = listOf(
            SeedReminder(3, "Respond to offer"),
        ),
    ),
    SeedApplication(
        company = "Halcyon Freight",
        role = "Android Platform Lead",
        status = AppStatus.APPLIED,
        daysAgoApplied = 8,
        nextActionOffsetDays = null,
        source = "Company site",
        postingUrl = "halcyonfreight.com/careers/platform-lead",
        notes = "Platform team lead role overseeing three mobile engineers. Found through the company's own " +
            "careers page.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.APPLIED, daysAgo = 8, note = "Application submitted"),
        ),
        contacts = emptyList(),
        reminders = emptyList(),
    ),
    SeedApplication(
        company = "Ostrom Analytics",
        role = "Android Engineer II",
        status = AppStatus.WISHLIST,
        daysAgoApplied = 12,
        nextActionOffsetDays = null,
        source = null,
        postingUrl = "ostromanalytics.com/careers/android-ii",
        notes = "Interesting data-visualization-heavy Android role. Want to research the team before applying.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.WISHLIST, daysAgo = 12, note = "Saved for later"),
        ),
        contacts = emptyList(),
        reminders = emptyList(),
    ),
    SeedApplication(
        company = "Kestrel Robotics",
        role = "Mobile Infrastructure",
        status = AppStatus.REJECTED,
        daysAgoApplied = 21,
        nextActionOffsetDays = null,
        source = "Referral",
        postingUrl = null,
        notes = "Good conversation throughout, but they promoted an internal candidate into the role.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.APPLIED, daysAgo = 21, note = "Referred by a former coworker"),
            SeedStatusEvent(AppStatus.SCREEN, daysAgo = 14, note = "Screen with the hiring manager"),
            SeedStatusEvent(AppStatus.REJECTED, daysAgo = 6, note = "Team went with an internal candidate"),
        ),
        contacts = listOf(
            SeedContact("Jules Ferreira", "Hiring manager", "jules.ferreira@kestrelrobotics.com"),
        ),
        reminders = emptyList(),
    ),
    SeedApplication(
        company = "Tidewater Health",
        role = "Senior Android Engineer",
        status = AppStatus.WITHDRAWN,
        daysAgoApplied = 30,
        nextActionOffsetDays = null,
        source = "Recruiter",
        postingUrl = null,
        notes = "Withdrew after accepting the Meridian Systems offer.",
        statusHistory = listOf(
            SeedStatusEvent(AppStatus.APPLIED, daysAgo = 30, note = "Sourced by an external recruiter"),
            SeedStatusEvent(AppStatus.WITHDRAWN, daysAgo = 1, note = "Withdrew after accepting another offer"),
        ),
        contacts = emptyList(),
        reminders = emptyList(),
    ),
)

fun todayDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

fun LocalDate.formatShort(): String {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${monthNames[month.ordinal]} $day"
}

