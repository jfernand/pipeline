/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

/** The "Follow-up overdue" callout shown at the top of a detail view, on both layouts. */
@Composable
fun OverdueBanner(message: String, dueDate: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(PlColors.overdueBg, RoundedCornerShape(4.dp))
            .border(1.dp, PlColors.overdueBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Notifications, null, tint = PlColors.brandPrimary, modifier = Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            MonoText("Follow-up overdue", size = 9.5f.sp, color = PlColors.brandPrimary)
            BodyText(message, size = 13.sp, color = PlColors.fgSecondary, modifier = Modifier.padding(top = 3.dp))
        }
        MonoText(dueDate, size = 9.5f.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
