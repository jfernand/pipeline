/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawTopBorder(color: Color, thickness: Dp = 1.dp): Modifier = drawWithContent {
    drawContent()
    drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = thickness.toPx())
}

fun Modifier.drawBottomBorder(color: Color, thickness: Dp = 1.dp): Modifier = drawWithContent {
    drawContent()
    drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = thickness.toPx())
}

fun Modifier.drawRightBorder(color: Color, thickness: Dp = 1.dp): Modifier = drawWithContent {
    drawContent()
    drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = thickness.toPx())
}

fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()), 0f),
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}
