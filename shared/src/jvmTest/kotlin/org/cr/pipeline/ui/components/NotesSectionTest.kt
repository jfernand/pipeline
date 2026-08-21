/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [NotesSection] takes its data and its save callback as plain parameters — no Koin, no
 * repository — so its edit/commit behavior is testable in isolation from everything that wires
 * it into a real detail screen (see DetailScreen.kt, DetailPane.kt).
 *
 * Uses `runComposeUiTest` rather than the JUnit4 `createComposeRule()`/`@get:Rule` pattern —
 * it's a plain function (`expect fun runComposeUiTest(... ): TestResult` in
 * `androidx.compose.ui.test.v2`), not tied to JUnit4's `TestRule` machinery, so it composes with
 * this project's usual `kotlin.test` `@Test` the same way `kotlinx.coroutines.test.runTest`
 * already does elsewhere (see docs/kotlin-best-practices.md's Testing section).
 */
@OptIn(ExperimentalTestApi::class)
class NotesSectionTest {

    @Test
    fun `shows the current notes as static text`() = runComposeUiTest {
        setContent { NotesSection(notes = "Talked to the recruiter Tuesday", onSave = {}) }
        onNodeWithText("Talked to the recruiter Tuesday").assertExists()
    }

    @Test
    fun `blank notes show a placeholder instead of empty text`() = runComposeUiTest {
        setContent { NotesSection(notes = "", onSave = {}) }
        onNodeWithText("Add notes").assertExists()
    }

    @Test
    fun `clicking the notes text turns it into an editable field`() = runComposeUiTest {
        setContent { NotesSection(notes = "Talked to the recruiter Tuesday", onSave = {}) }

        onNodeWithText("Talked to the recruiter Tuesday").performClick()
        waitForIdle()

        // The field starts pre-filled with the current text, and now accepts a set-text action —
        // static BodyText never does.
        onNode(hasSetTextAction()).assertExists()
        onNodeWithText("Talked to the recruiter Tuesday").assertExists()
    }

    @Test
    fun `editing then losing focus saves the new text once`() = runComposeUiTest {
        val saves = mutableListOf<String>()
        lateinit var focusManager: FocusManager
        setContent {
            focusManager = LocalFocusManager.current
            // Mirrors the real caller (DetailScreen/DetailPane): the repository write on save
            // eventually re-emits, feeding the new value back in as `notes`. Without that
            // round-trip here, the static display after commit would still show the stale prop.
            var currentNotes by remember { mutableStateOf("Original note") }
            NotesSection(
                notes = currentNotes,
                onSave = {
                    saves += it
                    currentNotes = it
                },
            )
        }

        onNodeWithText("Original note").performClick()
        waitForIdle()
        onNode(hasSetTextAction()).performTextReplacement("Called back, offer pending")
        runOnIdle { focusManager.clearFocus(force = true) }
        waitForIdle()

        assertEquals(listOf("Called back, offer pending"), saves)
        // Back to static display, not still an editable field.
        onNodeWithText("Called back, offer pending").assertExists()
    }

    @Test
    fun `losing focus without changing the text does not save`() = runComposeUiTest {
        var saved: String? = null
        lateinit var focusManager: FocusManager
        setContent {
            focusManager = LocalFocusManager.current
            NotesSection(notes = "Original note", onSave = { saved = it })
        }

        onNodeWithText("Original note").performClick()
        waitForIdle()
        onNode(hasSetTextAction()).assertExists()

        runOnIdle { focusManager.clearFocus(force = true) }
        waitForIdle()

        assertNull(saved)
    }

    @Test
    fun `clicking from empty starts an edit that saves whatever is typed`() = runComposeUiTest {
        val saves = mutableListOf<String>()
        lateinit var focusManager: FocusManager
        setContent {
            focusManager = LocalFocusManager.current
            NotesSection(notes = "", onSave = { saves += it })
        }

        onNodeWithText("Add notes").performClick()
        waitForIdle()
        onNode(hasSetTextAction()).performTextReplacement("First note")
        runOnIdle { focusManager.clearFocus(force = true) }
        waitForIdle()

        assertEquals(listOf("First note"), saves)
    }
}
