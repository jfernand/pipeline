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
        MonoText(status.label, size = fontSize, weight = FontWeight.Medium, color = status.color)
    }
}

@Composable
fun OverdueBadge(days: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(chipShape)
            .background(PlColors.brandPrimary)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        MonoText("${days}d overdue", size = 9.5f.sp, weight = FontWeight.SemiBold, color = PlColors.onBrand)
    }
}

@Composable
fun PlFilterChip(
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    dotColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .clip(chipShape)
            .background(if (active) PlColors.brandPrimary else Color.Transparent)
            .border(1.dp, if (active) PlColors.brandPrimary else PlColors.borderDefault, chipShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) Dot(color = dotColor, size = 6.dp)
        MonoText(label, size = 10.sp, weight = FontWeight.Medium, color = if (active) PlColors.onBrand else PlColors.fgSecondary)
    }
}

@Composable
fun Dot(modifier: Modifier = Modifier, color: Color = PlColors.brandPrimary, size: Dp = 7.dp) {
    Box(modifier.size(size).background(color, RoundedCornerShape(1.dp)))
}
