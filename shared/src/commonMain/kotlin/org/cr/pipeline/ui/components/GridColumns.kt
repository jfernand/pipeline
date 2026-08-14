package org.cr.pipeline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Lays [items] out in a simple `columns`-wide grid, matching the design's CSS grid usage. */
@Composable
fun <T> GridColumns(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 10.dp,
    content: @Composable (T) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { item ->
                    Box(Modifier.weight(1f)) { content(item) }
                }
                repeat(columns - row.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}
