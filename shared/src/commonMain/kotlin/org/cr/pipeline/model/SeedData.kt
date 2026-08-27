/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.model

import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

data class SeedApplication(
    val company: String,
    val role: String,
    val status: AppStatus,
    val daysAgoApplied: Int,
    val nextActionOffsetDays: Int?,
    val source: String?,
    val postingUrl: String?,
    val notes: String,
)

/**
 * The one source of demo data — PL-019's "Show fake data" seeds
 * [org.cr.pipeline.data.DemoSeedingEventLog] from this, once, the same way a real application
 * created through the Add/Edit form would be: one [org.cr.pipeline.sync.event.ApplicationCreated]
 * event per entry, via [toApplicationInput]. Status history, contacts, and reminders aren't part
 * of this shape — there's no event type that can set any of the three for a real application
 * either (see docs/product/reports/2026-08-26-codebase-review.typ), so a richer seed shape here
 * would just be state a real replay could never reproduce, the exact bug this feature exists to
 * close. [offsetDays] fields are relative to "today" so the app always looks current whenever the
 * demo log is actually created.
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
    ),
)

fun SeedApplication.toApplicationInput(today: LocalDate): ApplicationInput = ApplicationInput(
    company = company,
    role = role,
    status = status,
    dateApplied = today.minus(daysAgoApplied, DateTimeUnit.DAY),
    nextActionDate = nextActionOffsetDays?.let { today.plus(it, DateTimeUnit.DAY) },
    postingUrl = postingUrl,
    source = source,
    notes = notes,
)

fun todayDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

fun LocalDate.formatShort(): String {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${monthNames[month.ordinal]} $day"
}
