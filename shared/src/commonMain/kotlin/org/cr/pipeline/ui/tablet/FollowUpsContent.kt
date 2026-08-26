/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.FollowUpItem
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.koinInject

/**
 * PL-010: every overdue next-action date and overdue reminder, across every application, in one
 * place — the thing [org.cr.pipeline.data.JobApplicationRepository.observeFollowUps] exists for.
 * Self-contained (reads the repository itself) the same way [DevToolsContent] reads its own
 * sources, so the NavHost wiring in PipelineApp.kt stays a one-liner.
 */
@Composable
fun FollowUpsContent(onSelect: (Long) -> Unit, modifier: Modifier = Modifier) {
    val repository = koinInject<JobApplicationRepository>()
    val items by repository.observeFollowUps().collectAsState(initial = emptyList())

    Column(
        modifier
            .fillMaxSize()
            .background(PlColors.bgBase)
            .padding(horizontal = 40.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            DisplayText("Follow-ups", size = 31.sp)
            BodyText(
                "Every overdue next-action date and reminder, across your whole pipeline — not just the " +
                    "application you happen to have open.",
                size = 14.sp,
                color = PlColors.fgSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Overdue", count = items.size, accent = items.isNotEmpty())
            if (items.isEmpty()) {
                BodyText("Nothing overdue right now.", size = 13.sp, color = PlColors.fgMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.forEach { item -> FollowUpRow(item, onClick = { onSelect(item.applicationId) }) }
                }
            }
        }
    }
}

/** Same visual language as [org.cr.pipeline.ui.components.OverdueBanner] — company/role in place
 *  of that banner's fixed "Follow-up overdue" label, since every row on this screen already is
 *  one, and clickable, since this is the one place that list of rows gets browsed from. */
@Composable
private fun FollowUpRow(item: FollowUpItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PlColors.overdueBg, shape)
            .border(1.dp, PlColors.overdueBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Notifications, null, tint = PlColors.brandPrimary, modifier = Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            MonoText(
                "${item.company} · ${item.role}",
                size = 9.5f.sp,
                color = PlColors.brandPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BodyText(item.message, size = 13.sp, color = PlColors.fgSecondary, modifier = Modifier.padding(top = 3.dp))
        }
        MonoText(item.dueDate, size = 9.5f.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
