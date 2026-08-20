/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun SectionLabel(
    label: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    accent: Boolean = false,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MonoText(label, size = 10.sp, weight = FontWeight.SemiBold, color = if (accent) PlColors.brandPrimary else PlColors.fgMuted)
        if (count != null) MonoText("· $count", size = 10.sp, color = PlColors.fgMuted)
        Spacer(Modifier.weight(1f).height(1.dp).background(PlColors.borderDefault))
    }
}
