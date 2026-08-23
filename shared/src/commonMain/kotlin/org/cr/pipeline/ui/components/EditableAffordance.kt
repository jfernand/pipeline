/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cr.pipeline.ui.theme.PlColors

/**
 * States for [Modifier.editableAffordance] — see docs/design/editable-affordance (design 1f,
 * "State ladder"). [Editing] is the one state that closes the rectangle; every other state only
 * marks corners.
 */
enum class EditableAffordanceState {
    /** Editable, not being interacted with: quiet steel dots at the four corners. */
    Rest,

    /** Pointer is over it: amber corner chevrons plus a faint amber tint. */
    Hover,

    /** A live text cursor is in it: a full amber box, not just corners. */
    Editing,

    /** Editable but unset: muted chevrons around placeholder text. */
    Empty,

    /** Failed validation: red chevrons plus a faint red tint. */
    Rejected,

    /** Not editable right now: dim steel dots, no interaction. */
    Locked,
}

/**
 * Design 1e, "Dots, resting" — draws [state]'s corner marks (and, for [EditableAffordanceState.Hover],
 * [EditableAffordanceState.Editing] and [EditableAffordanceState.Rejected], a background tint)
 * directly behind the content, at the modifier's own layout bounds. Apply padding *before* this
 * modifier in the chain (i.e. `.editableAffordance(state).padding(...)`) so the marks sit with
 * some breathing room from the text rather than touching the glyphs.
 *
 * [restColor] only affects [EditableAffordanceState.Rest] — defaults to the brand amber, since
 * plain grey dots read as inconspicuous; pass [PlColors.fgMuted] at a call site that wants the
 * quieter look back.
 */
fun Modifier.editableAffordance(
    state: EditableAffordanceState,
    dotRadius: Dp = 1.5.dp,
    armLength: Dp = 5.dp,
    strokeWidth: Dp = 1.dp,
    restColor: Color = PlColors.brandPrimary,
): Modifier = drawWithContent {
    val sw = strokeWidth.toPx()
    when (state) {
        EditableAffordanceState.Rest ->
            drawCornerDots(restColor, dotRadius)

        EditableAffordanceState.Hover -> {
            drawRect(PlColors.brandPrimary.copy(alpha = 0.10f))
            drawCornerChevrons(PlColors.brandPrimary, armLength, sw)
        }

        EditableAffordanceState.Editing -> {
            drawRect(PlColors.brandPrimary.copy(alpha = 0.16f))
            drawRect(PlColors.brandPrimary, style = Stroke(sw))
        }

        EditableAffordanceState.Empty ->
            drawCornerChevrons(PlColors.fgMuted, armLength, sw)

        EditableAffordanceState.Rejected -> {
            drawRect(PlColors.danger.copy(alpha = 0.12f))
            drawCornerChevrons(PlColors.danger, armLength, sw)
        }

        EditableAffordanceState.Locked ->
            drawCornerDots(PlColors.fgDisabled, dotRadius)
    }
    drawContent()
}

private fun DrawScope.drawCornerDots(color: Color, radius: Dp) {
    val r = radius.toPx()
    for (corner in corners()) drawCircle(color = color, radius = r, center = corner)
}

private fun DrawScope.drawCornerChevrons(color: Color, armLength: Dp, strokeWidthPx: Float) {
    val arm = armLength.toPx()
    val w = size.width
    val h = size.height
    // Each corner gets two arms — one running along each edge that meets there — so a
    // one-word run still reads as a rectangle's corner, not a stray tick mark.
    val chevrons = listOf(
        Offset(0f, 0f) to listOf(Offset(arm, 0f), Offset(0f, arm)),
        Offset(w, 0f) to listOf(Offset(w - arm, 0f), Offset(w, arm)),
        Offset(0f, h) to listOf(Offset(arm, h), Offset(0f, h - arm)),
        Offset(w, h) to listOf(Offset(w - arm, h), Offset(w, h - arm)),
    )
    for ((corner, arms) in chevrons) {
        for (armEnd in arms) drawLine(color, corner, armEnd, strokeWidthPx)
    }
}

private fun DrawScope.corners(): List<Offset> = listOf(
    Offset(0f, 0f),
    Offset(size.width, 0f),
    Offset(0f, size.height),
    Offset(size.width, size.height),
)

/**
 * Derives an [EditableAffordanceState] from field-level flags plus live hover — the state a call
 * site would otherwise have to compute by hand every time it wants 1e's rest→hover→editing ladder.
 * Precedence: [locked] beats [editing] beats [rejected] beats [empty] beats hover beats rest.
 */
@Composable
fun rememberEditableAffordanceState(
    interactionSource: MutableInteractionSource,
    editing: Boolean = false,
    empty: Boolean = false,
    rejected: Boolean = false,
    locked: Boolean = false,
): EditableAffordanceState {
    val hovered by interactionSource.collectIsHoveredAsState()
    return remember(editing, empty, rejected, locked, hovered) {
        when {
            locked -> EditableAffordanceState.Locked
            editing -> EditableAffordanceState.Editing
            rejected -> EditableAffordanceState.Rejected
            empty -> EditableAffordanceState.Empty
            hovered -> EditableAffordanceState.Hover
            else -> EditableAffordanceState.Rest
        }
    }
}

/**
 * Wraps [content] with 1e's corner-marked editable affordance and, unless [locked], makes it
 * hoverable and clickable. This is the small-item alternative to [Field]'s always-visible box —
 * for a value that reads as plain text until you're on it, not a form input sitting in the layout
 * all the time.
 *
 * [restColor] only affects the resting-state dots — see [Modifier.editableAffordance].
 */
@Composable
fun EditableAffordanceBox(
    modifier: Modifier = Modifier,
    editing: Boolean = false,
    empty: Boolean = false,
    rejected: Boolean = false,
    locked: Boolean = false,
    restColor: Color = PlColors.brandPrimary,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state = rememberEditableAffordanceState(interactionSource, editing, empty, rejected, locked)
    Box(
        modifier
            .hoverable(interactionSource, enabled = !locked)
            .then(
                if (onClick != null && !locked) {
                    Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .editableAffordance(state, restColor = restColor)
            .padding(horizontal = 2.dp, vertical = 3.dp),
    ) {
        content()
    }
}
