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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.PlColors

private val buttonShape = RoundedCornerShape(2.dp)

@Composable
fun PlPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Dp = 48.dp,
    icon: ImageVector? = null,
) {
    Surface(onClick = onClick, modifier = modifier.height(height), color = PlColors.brandPrimary, shape = buttonShape) {
        Row(
            Modifier.fillMaxHeight().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon?.let { Icon(it, null, tint = PlColors.onBrand, modifier = Modifier.size(16.dp)) }
            BodyText(text, size = 14.5f.sp, weight = FontWeight.SemiBold, color = PlColors.onBrand)
        }
    }
}

@Composable
fun PlSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    height: Dp = 48.dp,
    icon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(height),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, PlColors.borderStrong),
        shape = buttonShape,
    ) {
        Row(
            Modifier.fillMaxHeight().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon?.let { Icon(it, null, tint = PlColors.fgPrimary, modifier = Modifier.size(16.dp)) }
            BodyText(text, size = 14.5f.sp, weight = FontWeight.Medium, color = PlColors.fgPrimary)
        }
    }
}

@Composable
fun PlIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    size: Dp = 44.dp,
    iconSize: Dp = 16.dp,
    tint: Color = PlColors.fgSecondary,
    bordered: Boolean = false,
) {
    Box(
        modifier
            .size(size)
            .clip(buttonShape)
            .then(if (bordered) Modifier.border(1.dp, PlColors.borderDefault, buttonShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(iconSize))
    }
}
