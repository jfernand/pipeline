package org.cr.pipeline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors
import org.cr.pipeline.ui.theme.drawBottomBorder

@Composable
fun PipeTopBar(
    title: String,
    leftIcon: ImageVector,
    modifier: Modifier = Modifier,
    onLeftClick: () -> Unit = {},
    rightActions: List<Pair<ImageVector, () -> Unit>> = emptyList(),
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBottomBorder(PipeColors.borderSubtle)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PipeIconButton(leftIcon, onClick = onLeftClick, size = 44.dp, iconSize = 20.dp, tint = PipeColors.fgSecondary)
        DisplayText(
            title,
            size = 21.sp,
            letterSpacing = 0.02f.em,
            modifier = Modifier.weight(1f).padding(start = 2.dp),
        )
        rightActions.forEach { (icon, onClick) ->
            PipeIconButton(icon, onClick = onClick, size = 44.dp, iconSize = 20.dp, tint = PipeColors.fgSecondary)
        }
        if (actionLabel != null) {
            Box(
                Modifier.height(44.dp).clickable(onClick = onAction).padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                MonoText(actionLabel, size = 11.sp, weight = FontWeight.SemiBold, color = PipeColors.brandPrimary)
            }
        }
    }
}
