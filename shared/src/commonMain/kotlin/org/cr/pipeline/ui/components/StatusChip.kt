/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.dashedBorder

private val chipShape = RoundedCornerShape(2.dp)

@Composable
fun StatusChip(status: AppStatus, modifier: Modifier = Modifier, small: Boolean = false) {
    val fontSize = if (small) 9.5f.sp else 10.5f.sp
    val paddingV = if (small) 4.dp else 5.dp
    val paddingH = if (small) 6.dp else 8.dp
    val borderColor = status.color.copy(alpha = 0.35f)
    Box(
        modifier
            .clip(chipShape)
            .background(status.color.copy(alpha = 0.12f))
            .then(
                if (status == AppStatus.WITHDRAWN) {
                    Modifier.dashedBorder(borderColor, cornerRadius = 2.dp)
                } else {
                    Modifier.border(1.dp, borderColor, chipShape)
                },
            )
            .padding(horizontal = paddingH, vertical = paddingV),
    ) {
        MonoText(status.label, size = fontSize, color = status.color)
    }
}

/** Meant to sit as a small tab over the top edge of the [StatusChip] it's paired with — see
 *  [AppCard], its only caller — not as a same-size badge occupying its own space in the row. */
@Composable
fun OverdueBadge(days: Int, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(chipShape)
            .background(PlColors.brandPrimary)
            .padding(horizontal = 1.dp, vertical = 0.dp),
    ) {
        MonoText(
            "${days}d",
            size = 8.sp,
            weight = FontWeight.SemiBold,
            color = PlColors.onBrand,
            letterSpacing = 0.sp,
            lineHeight = 8.sp,
        )
    }
}

/** [negative] is a third, independent look on top of [active] — e.g. PL-023's status filter
 *  chips, where a status can be excluded ("negative") rather than just selected. Takes precedence
 *  over [active] when both are somehow true, but callers are expected to only ever set one. */
@Composable
fun PlFilterChip(
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    negative: Boolean = false,
    dotColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    val accent = if (negative) PlColors.danger else PlColors.brandPrimary
    Row(
        modifier
            .clip(chipShape)
            .background(if (active || negative) accent else Color.Transparent)
            .border(1.dp, if (active || negative) accent else PlColors.borderDefault, chipShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) Dot(color = dotColor, size = 6.dp)
        MonoText(
            label,
            size = 10.sp,
            // fgPrimary rather than onBrand for the negative case — danger's dark, saturated red
            // needs a light foreground for contrast, the opposite of brandPrimary's light amber.
            color = when {
                negative -> PlColors.fgPrimary
                active -> PlColors.onBrand
                else -> PlColors.fgSecondary
            },
        )
    }
}

@Composable
fun Dot(modifier: Modifier = Modifier, color: Color = PlColors.brandPrimary, size: Dp = 7.dp) {
    Box(modifier.size(size).background(color, RoundedCornerShape(1.dp)))
}
