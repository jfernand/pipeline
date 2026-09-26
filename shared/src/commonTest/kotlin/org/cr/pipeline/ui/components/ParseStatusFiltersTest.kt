/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import org.cr.pipeline.model.AppStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseStatusFiltersTest {
    @Test
    fun `no parameters means no filters`() {
        assertEquals(emptyMap(), parseStatusFilters(null, null))
        assertEquals(emptyMap(), parseStatusFilters("", ""))
    }

    @Test
    fun `status names become required chips and exclude names excluded ones`() {
        assertEquals(
            mapOf(
                AppStatus.INTERVIEW to StatusFilterMode.POSITIVE,
                AppStatus.OFFER to StatusFilterMode.POSITIVE,
                AppStatus.REJECTED to StatusFilterMode.NEGATIVE,
            ),
            parseStatusFilters("interviewing,offer", "rejected"),
        )
    }

    @Test
    fun `a name matches by label or enum name, ignoring case and separators`() {
        val required = parseStatusFilters("Interviewing,screen,phone-screen,Phone Screen,WISHLIST", null)
        assertEquals(setOf(AppStatus.INTERVIEW, AppStatus.SCREEN, AppStatus.WISHLIST), required.keys)
        assertEquals(setOf(AppStatus.INTERVIEW), parseStatusFilters("interview", null).keys)
    }

    @Test
    fun `unknown names are dropped rather than failing the link`() {
        assertEquals(mapOf(AppStatus.OFFER to StatusFilterMode.POSITIVE), parseStatusFilters("ofer,offer,,", "nope"))
    }

    @Test
    fun `a status both required and excluded ends up excluded`() {
        assertEquals(mapOf(AppStatus.OFFER to StatusFilterMode.NEGATIVE), parseStatusFilters("offer", "offer"))
    }
}
