/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.components.PlIconButton
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder

/**
 * Full-screen (minus nav rail) application detail, reached by navigating from the list. A real
 * device's landscape split-pane width isn't enough to show the list and detail side by side
 * without squashing one of them, so the detail view now owns the whole content area instead.
 */
@Composable
fun TabletDetailScreen(
    applicationId: Long?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onUpdateStatus: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .drawBottomBorder(PlColors.borderSubtle)
                .padding(start = 12.dp, top = 12.dp, end = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlIconButton(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack, size = 36.dp, iconSize = 18.dp)
            MonoText("Applications", size = 10.sp, color = PlColors.fgMuted)
        }
        DetailPane(
            applicationId = applicationId,
            onUpdateStatus = onUpdateStatus,
            onEdit = onEdit,
            modifier = Modifier.weight(1f),
        )
    }
}
