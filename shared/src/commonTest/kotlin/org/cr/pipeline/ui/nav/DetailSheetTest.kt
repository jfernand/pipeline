/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailSheetTest {
    @Test
    fun `a sheet name matches ignoring case`() {
        assertEquals(DetailSheet.STATUS, "status".toDetailSheet())
        assertEquals(DetailSheet.STATUS, "Status".toDetailSheet())
        assertEquals(DetailSheet.CONTACT, "contact".toDetailSheet())
    }

    @Test
    fun `no sheet, or one that doesn't exist, opens none`() {
        assertNull(null.toDetailSheet())
        assertNull("".toDetailSheet())
        assertNull("edit".toDetailSheet())
    }

    @Test
    fun `a plain detail route carries no sheet`() {
        assertNull(DetailRoute(42).sheet)
    }
}
