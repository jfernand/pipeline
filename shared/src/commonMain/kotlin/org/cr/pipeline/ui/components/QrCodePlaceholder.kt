package org.cr.pipeline.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cr.pipeline.ui.theme.PipeColors

/**
 * Deterministic pseudo-QR pattern (not a scannable code) matching the design mock's approach:
 * a seeded 25x25 grid with three finder squares, rendered on a paper background.
 */
@Composable
fun QrCodePlaceholder(modifier: Modifier = Modifier, qrSize: Dp = 176.dp) {
    Canvas(modifier.size(qrSize)) {
        val n = 25
        val cellPx = size.width / n
        val ink = Color(0xFF0E0E0E)
        drawRect(color = PipeColors.paper)

        var seed = 20260624L
        fun rnd(): Double {
            seed = (seed * 1103515245L + 12345L) and 0x7fffffffL
            return seed / 0x7fffffff.toDouble()
        }
        fun isFinder(r: Int, c: Int) = (r < 8 && c < 8) || (r < 8 && c > n - 9) || (r > n - 9 && c < 8)

        for (r in 0 until n) {
            for (c in 0 until n) {
                if (isFinder(r, c)) continue
                if (rnd() > 0.52) {
                    drawRect(color = ink, topLeft = Offset(c * cellPx, r * cellPx), size = Size(cellPx, cellPx))
                }
            }
        }

        fun eye(x: Int, y: Int) {
            drawRect(color = ink, topLeft = Offset(x * cellPx, y * cellPx), size = Size(cellPx * 7, cellPx * 7))
            drawRect(color = PipeColors.paper, topLeft = Offset((x + 1) * cellPx, (y + 1) * cellPx), size = Size(cellPx * 5, cellPx * 5))
            drawRect(color = ink, topLeft = Offset((x + 2) * cellPx, (y + 2) * cellPx), size = Size(cellPx * 3, cellPx * 3))
        }
        eye(0, 0)
        eye(n - 7, 0)
        eye(0, n - 7)
    }
}
