/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder

@Composable
fun SettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    icon: ImageVector? = null,
    chevron: Boolean = true,
    /** Replaces the chevron with a small mono label (e.g. "ON"/"OFF") — for rows that toggle in
     *  place on tap rather than navigating anywhere. */
    trailingText: String? = null,
    trailingColor: Color = PlColors.fgMuted,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .drawBottomBorder(PlColors.borderSubtle)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .defaultMinSize(minHeight = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = PlColors.fgMuted, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            BodyText(label, size = 14.5f.sp, color = PlColors.fgPrimary)
            if (value != null) MonoText(value, size = 9.sp, modifier = Modifier.padding(top = 3.dp))
        }
        if (trailingText != null) {
            MonoText(trailingText, size = 9.5f.sp, color = trailingColor)
        } else if (chevron) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
        }
    }
}
