/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.ContactSummary
import org.cr.pipeline.model.StatusHistoryEntry
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType

/** The "Status history" timeline block shown on both phone and tablet detail views. */
@Composable
fun StatusHistorySection(statusHistory: List<StatusHistoryEntry>, modifier: Modifier = Modifier) {
    if (statusHistory.isEmpty()) return
    DetailSection(
        label = "Status history",
        right = { MonoText(changesLabel(statusHistory.size), size = 9.sp) },
        modifier = modifier,
    ) {
        Timeline(entries = statusHistory.map { TimelineEntry(it.status, it.date, it.note, it.current) })
    }
}

/**
 * The "Notes" block shown on both phone and tablet detail views. Tap the text to edit it in
 * place — the corner-marked affordance (PL-032) signals it's editable instead of a persistent
 * pencil icon; [onSave] fires once, with the final text, when editing ends by losing focus, by
 * pressing Return, or by tapping a soft keyboard's Done key.
 *
 * Two separate mechanisms commit on Return/Done, because no single one covers both input paths:
 * `onPreviewKeyEvent` sees hardware key presses (desktop, a physical/Bluetooth keyboard on
 * Android) — Return commits there unless Shift is also held, so Shift+Return still inserts a
 * newline. Android's on-screen keyboard, though, doesn't dispatch a `KeyEvent` for its own Return
 * key on a multi-line field by default — it just inserts `\n` straight into the composition,
 * `onPreviewKeyEvent` never fires — so `keyboardOptions`/`keyboardActions` (the IME-action
 * mechanism) is what makes it show a Done glyph instead and actually commit.
 *
 * Cancel — discard the draft, restore the original text, no [onSave] call — has no keyboard
 * equivalent on touch: there's no Escape key on a soft keyboard, and the one IME action slot is
 * already spoken for by Done. So Escape (hardware keyboards only) and a small ✕ button next to
 * the field (visible while editing, works everywhere) are two separate, non-overlapping paths to
 * the same [cancel] call, not one mechanism covering both like commit's does.
 *
 * The ✕ button is marked `focusProperties { canFocus = false }`. Without that, tapping it — a
 * plain `clickable` — first steals focus from the field (`clickable` requests focus for itself
 * on press, ahead of its own `onClick`), which blurs the field and commits *before* the button's
 * click ever fires, unmounting the button mid-gesture so [cancel] never runs at all — clicking
 * Cancel silently saved instead. No amount of reordering commit-on-blur fixes this: the button's
 * press alone triggers it, strictly before release/click, so the field simply has to keep focus
 * throughout the tap for [cancel] to ever run.
 */
@Composable
fun NotesSection(notes: String, onSave: (String) -> Unit, modifier: Modifier = Modifier) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(notes) { mutableStateOf(notes) }
    // BasicTextField's own onFocusChanged fires once, isFocused = false, the instant it first
    // composes — before the LaunchedEffect below ever gets to request focus. Treating every
    // "not focused" callback as a blur closed the field again immediately on every click, so
    // this only counts a callback as a real blur once the field has actually been focused.
    var hasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Shared by the blur path and the Return-key path; guarded by `editing` so whichever fires
    // second — unmounting the field on commit can still trigger one more onFocusChanged(false) —
    // is a no-op rather than a duplicate onSave.
    fun commit() {
        if (!editing) return
        editing = false
        if (draft != notes) onSave(draft)
    }

    // Unmounting the field on cancel triggers the same possible extra onFocusChanged(false) as
    // commit() does — guarded by `editing` for the same reason, so it can't also fire commit()'s
    // save on the way out.
    fun cancel() {
        if (!editing) return
        editing = false
        draft = notes
    }

    DetailSection(label = "Notes", modifier = modifier) {
        EditableAffordanceBox(
            empty = !editing && notes.isBlank(),
            editing = editing,
            onClick = if (editing) {
                null
            } else {
                {
                    draft = notes
                    hasFocused = false
                    editing = true
                }
            },
        ) {
            if (editing) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        textStyle = TextStyle(
                            fontFamily = PlType.body(),
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = PlColors.fgPrimary,
                        ),
                        cursorBrush = SolidColor(PlColors.brandPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { commit() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type != KeyEventType.KeyDown) {
                                    false
                                } else if (keyEvent.key == Key.Escape) {
                                    cancel()
                                    true
                                } else if (
                                    (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) &&
                                    !keyEvent.isShiftPressed
                                ) {
                                    commit()
                                    true
                                } else {
                                    false
                                }
                            }
                            .onFocusChanged { focus ->
                                if (focus.isFocused) {
                                    hasFocused = true
                                } else if (hasFocused) {
                                    commit()
                                }
                            },
                    )
                    PlIconButton(
                        Icons.Filled.Close,
                        onClick = ::cancel,
                        modifier = Modifier.focusProperties { canFocus = false },
                        size = 28.dp,
                        iconSize = 14.dp,
                        tint = PlColors.fgMuted,
                        contentDescription = "Cancel",
                    )
                }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                BodyText(
                    notes.ifBlank { "Add notes" },
                    size = 13.5f.sp,
                    color = if (notes.isBlank()) PlColors.fgMuted else PlColors.fgSecondary,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

/** The "Contacts" list block shown on both phone and tablet detail views. [onAdd] is always
 *  wired, including when [contacts] is empty — that's the only way to add the first one. */
@Composable
fun ContactsSection(contacts: List<ContactSummary>, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    DetailSection(
        label = "Contacts",
        right = {
            PlIconButton(
                Icons.Filled.Add,
                onClick = onAdd,
                size = 24.dp,
                iconSize = 16.dp,
                tint = PlColors.fgMuted,
                contentDescription = "Add contact",
            )
        },
        modifier = modifier,
    ) {
        if (contacts.isEmpty()) {
            BodyText("No contacts yet", size = 13.5f.sp, color = PlColors.fgMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                contacts.forEach { contact ->
                    ContactRow(ContactInfo(contact.name.contactInitials(), contact.name, contact.role, contact.email))
                }
            }
        }
    }
}
