/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

// Mirrors AddEditScreen's SOURCE_OPTIONS ("Referral", "LinkedIn", "Company site", "Recruiter",
// "Other") — anything that doesn't match one of the named options (older data, free text) falls
// back to the same glyph as "Other" rather than showing nothing.
private fun sourceIcon(source: String): ImageVector = when (source) {
    "Referral" -> Icons.Filled.Groups
    "LinkedIn" -> Icons.Filled.Public
    "Company site" -> Icons.Filled.Business
    "Recruiter" -> Icons.Filled.PersonSearch
    else -> Icons.Filled.MoreHoriz
}

@Composable
fun AppCard(
    app: JobApplication,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(4.dp)
    val borderColor = when {
        selected -> PlColors.brandPrimary
        app.overdueDays != null -> PlColors.overdueBorder
        else -> PlColors.borderDefault
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PlColors.bgRaised)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                BodyText(
                    app.company,
                    size = 16.5f.sp,
                    weight = FontWeight.SemiBold,
                    color = PlColors.fgPrimary,
                    lineHeight = 20.sp
                )
                BodyText(
                    app.role,
                    size = 13.5f.sp,
                    color = PlColors.fgSecondary,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            MonoText("${app.daysAgo}d", size = 10.5f.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                StatusChip(app.status)
                // Tabs over the chip's top-right corner rather than sitting beside it as a
                // second same-size badge — the offset is roughly half of OverdueBadge's own
                // height so it visibly overlaps the top edge instead of just touching it.
                app.overdueDays?.let {
                    OverdueBadge(
                        it, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-6).dp, y = (-5).dp)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            val (label, ago) = app.activity
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                MonoText(label, size = 9.5f.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                MonoText(ago, size = 9.5f.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
