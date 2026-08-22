/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.ui.components.ContactsSection
import org.cr.pipeline.ui.components.DetailSection
import org.cr.pipeline.ui.components.DimmedOverlay
import org.cr.pipeline.ui.components.NotesSection
import org.cr.pipeline.ui.components.OverdueBanner
import org.cr.pipeline.ui.components.PlIconButton
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.PostingLinkRow
import org.cr.pipeline.ui.components.StatusChip
import org.cr.pipeline.ui.components.StatusHistorySection
import org.cr.pipeline.ui.components.rememberApplicationDetail
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.koinInject

/** Stateful: owns the [applicationId] -> [ApplicationDetail] lookup and hoists it into
 *  [DetailScreenContent], which does the actual rendering. */
@Composable
fun DetailScreen(
    applicationId: Long?,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onBack: () -> Unit = {},
    onUpdate: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    val detail = rememberApplicationDetail(applicationId)
    val repository = koinInject<JobApplicationRepository>()
    val scope = rememberCoroutineScope()
    DetailScreenContent(
        detail,
        modifier,
        dimmed,
        onBack,
        onUpdate,
        onEdit,
        onSaveNotes = { notes ->
            val id = applicationId ?: return@DetailScreenContent
            scope.launch {
                val input = repository.getApplicationInput(id) ?: return@launch
                repository.saveApplication(id, input.copy(notes = notes))
            }
        },
    )
}

/** Stateless: renders whatever [detail] it's given, with no knowledge of where it came from. */
@Composable
private fun DetailScreenContent(
    detail: ApplicationDetail?,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onBack: () -> Unit = {},
    onUpdate: () -> Unit = {},
    onEdit: () -> Unit = {},
    onSaveNotes: (String) -> Unit = {},
) {
    DimmedOverlay(dimmed, modifier.background(PlColors.bgBase)) {
        if (detail == null) {
            Box(Modifier.fillMaxSize()) {
                PlTopBar(title = "Application", leftIcon = Icons.AutoMirrored.Filled.ArrowBack, onLeftClick = onBack)
            }
            return@DimmedOverlay
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PlTopBar(
                title = "Application",
                leftIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onLeftClick = onBack,
                rightActions = listOf(Icons.Filled.Edit to onEdit, Icons.Filled.Delete to {}),
            )
            DetailHeader(detail, onUpdate)
            val overdueReminder = detail.reminders.firstOrNull { it.overdue }
            if (overdueReminder != null) {
                OverdueBanner(
                    message = overdueReminder.message,
                    dueDate = overdueReminder.dueDate,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            StatusHistorySection(detail.statusHistory, modifier = Modifier.padding(horizontal = 16.dp))
            ContactsSection(detail.contacts, modifier = Modifier.padding(horizontal = 16.dp))
            NotesSection(detail.notes, onSaveNotes, modifier = Modifier.padding(horizontal = 16.dp))
            PostingSection(detail, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun DetailHeader(detail: ApplicationDetail, onUpdate: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            DisplayText(detail.company, size = 30.sp, lineHeight = 32.sp)
            BodyText(detail.role, size = 15.sp, color = PlColors.fgSecondary, modifier = Modifier.padding(top = 4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(detail.status)
            MonoText(
                "${detail.daysSinceActivity}d since activity",
                size = 9.5f.sp,
                color = PlColors.fgMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlPrimaryButton("Update status", onClick = onUpdate, height = 46.dp, modifier = Modifier.weight(1f))
            PlIconButton(Icons.AutoMirrored.Filled.OpenInNew, size = 46.dp, bordered = true, tint = PlColors.fgPrimary, iconSize = 18.dp)
        }
    }
}

@Composable
private fun PostingSection(detail: ApplicationDetail, modifier: Modifier = Modifier) {
    if (detail.postingUrl == null) return
    DetailSection(label = "Posting", modifier = modifier) {
        PostingLinkRow(detail.postingUrl)
        if (detail.source != null || detail.dateApplied != null) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                detail.source?.let {
                    MonoText("Source: $it", size = 9.sp, color = PlColors.fgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                detail.dateApplied?.let {
                    MonoText("Added $it", size = 9.sp, color = PlColors.fgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
