/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [rememberApplicationListFilter] and [StatusFilterChips] take their data and callbacks as plain
 * parameters — no Koin, no repository — so they're testable in isolation the same way
 * [NotesSection] is (see NotesSectionTest's own doc comment for why `runComposeUiTest` rather
 * than a JUnit4 rule).
 */
@OptIn(ExperimentalTestApi::class)
class StatusFilterChipsTest {
    private fun application(status: AppStatus, id: Long = status.ordinal.toLong()) = JobApplication(
        id = id,
        company = "Company $id",
        role = "Role",
        status = status,
        daysAgo = 1,
        activity = "Applied" to "1d",
    )

    private val applications = AppStatus.entries.map { application(it) }

    @Test
    fun `an unset filter shows every application`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        setContent { filter = rememberApplicationListFilter(applications) }

        assertEquals(applications.size, filter.followUp.size + filter.rest.size)
        assertEquals(emptyMap(), filter.statusFilters)
    }

    @Test
    fun `initial query and status filters preset what the list shows`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        val applications = listOf(
            application(AppStatus.INTERVIEW, id = 1).copy(company = "Acme"),
            application(AppStatus.INTERVIEW, id = 2).copy(company = "Globex"),
            application(AppStatus.REJECTED, id = 3).copy(company = "Acme Labs"),
        )
        setContent {
            filter = rememberApplicationListFilter(
                applications,
                initialQuery = "acme",
                initialStatusFilters = mapOf(AppStatus.REJECTED to StatusFilterMode.NEGATIVE),
            )
        }

        assertEquals("acme", filter.query)
        assertEquals(listOf(1L), (filter.followUp + filter.rest).map { it.id })
    }

    @Test
    fun `cycling one status through positive then negative then back to unset`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        setContent { filter = rememberApplicationListFilter(applications) }

        runOnIdle { filter.onStatusFilterCycle(AppStatus.OFFER) }
        waitForIdle()
        assertEquals(mapOf(AppStatus.OFFER to StatusFilterMode.POSITIVE), filter.statusFilters)
        assertEquals(setOf(AppStatus.OFFER), (filter.followUp + filter.rest).map { it.status }.toSet())

        runOnIdle { filter.onStatusFilterCycle(AppStatus.OFFER) }
        waitForIdle()
        assertEquals(mapOf(AppStatus.OFFER to StatusFilterMode.NEGATIVE), filter.statusFilters)
        val visibleAfterNegative = (filter.followUp + filter.rest).map { it.status }.toSet()
        assertEquals(AppStatus.entries.toSet() - AppStatus.OFFER, visibleAfterNegative)

        runOnIdle { filter.onStatusFilterCycle(AppStatus.OFFER) }
        waitForIdle()
        assertEquals(emptyMap(), filter.statusFilters)
        assertEquals(applications.size, filter.followUp.size + filter.rest.size)
    }

    @Test
    fun `multiple positive statuses combine with OR`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        setContent { filter = rememberApplicationListFilter(applications) }

        runOnIdle {
            filter.onStatusFilterCycle(AppStatus.INTERVIEW)
            filter.onStatusFilterCycle(AppStatus.OFFER)
        }
        waitForIdle()

        val visible = (filter.followUp + filter.rest).map { it.status }.toSet()
        assertEquals(setOf(AppStatus.INTERVIEW, AppStatus.OFFER), visible)
    }

    @Test
    fun `multiple negative statuses combine to exclude all of them`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        setContent { filter = rememberApplicationListFilter(applications) }

        // Two taps each: positive, then negative.
        runOnIdle {
            filter.onStatusFilterCycle(AppStatus.REJECTED)
            filter.onStatusFilterCycle(AppStatus.REJECTED)
            filter.onStatusFilterCycle(AppStatus.WITHDRAWN)
            filter.onStatusFilterCycle(AppStatus.WITHDRAWN)
        }
        waitForIdle()

        assertEquals(
            mapOf(AppStatus.REJECTED to StatusFilterMode.NEGATIVE, AppStatus.WITHDRAWN to StatusFilterMode.NEGATIVE),
            filter.statusFilters,
        )
        val visible = (filter.followUp + filter.rest).map { it.status }.toSet()
        assertEquals(AppStatus.entries.toSet() - AppStatus.REJECTED - AppStatus.WITHDRAWN, visible)
    }

    @Test
    fun `a positive requirement together with an unrelated negative excludes just the negative one`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        setContent { filter = rememberApplicationListFilter(applications) }

        // OFFER: one tap (positive). WITHDRAWN: two taps (negative). Since a real application's
        // status can't be both at once, the negative is redundant here, but the combined
        // predicate should still just resolve to "OFFER only" without throwing or double-counting.
        runOnIdle {
            filter.onStatusFilterCycle(AppStatus.OFFER)
            filter.onStatusFilterCycle(AppStatus.WITHDRAWN)
            filter.onStatusFilterCycle(AppStatus.WITHDRAWN)
        }
        waitForIdle()

        val visible = (filter.followUp + filter.rest).map { it.status }.toSet()
        assertEquals(setOf(AppStatus.OFFER), visible)
    }

    @Test
    fun `clearing resets every chip to unset`() = runComposeUiTest {
        lateinit var filter: ApplicationListFilterState
        setContent { filter = rememberApplicationListFilter(applications) }

        runOnIdle {
            filter.onStatusFilterCycle(AppStatus.OFFER)
            filter.onStatusFilterCycle(AppStatus.REJECTED)
            filter.onStatusFilterCycle(AppStatus.REJECTED)
        }
        waitForIdle()
        runOnIdle { filter.onClearStatusFilters() }
        waitForIdle()

        assertEquals(emptyMap(), filter.statusFilters)
        assertEquals(applications.size, filter.followUp.size + filter.rest.size)
    }

    @Test
    fun `tapping the All chip calls onClear, not onCycle`() = runComposeUiTest {
        var cleared = false
        val cycled = mutableListOf<AppStatus>()
        setContent {
            // StatusFilterChips emits flat children — every real caller wraps it in a Row/FlowRow;
            // without one here the chips would stack on top of each other at the same position.
            Row {
                StatusFilterChips(
                    totalCount = 7,
                    filters = mapOf(AppStatus.OFFER to StatusFilterMode.POSITIVE),
                    onCycle = { cycled += it },
                    onClear = { cleared = true },
                )
            }
        }

        onNodeWithText("ALL · 7").performClick()
        waitForIdle()

        assertEquals(true, cleared)
        assertEquals(emptyList(), cycled)
    }

    @Test
    fun `tapping a status chip cycles that status, not others`() = runComposeUiTest {
        val cycled = mutableListOf<AppStatus>()
        setContent {
            Row {
                StatusFilterChips(totalCount = 7, filters = emptyMap(), onCycle = { cycled += it }, onClear = {})
            }
        }

        onNodeWithText(AppStatus.OFFER.label, ignoreCase = true).performClick()
        waitForIdle()

        assertEquals(listOf(AppStatus.OFFER), cycled)
    }
}
