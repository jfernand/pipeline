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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.components.ContactInfo
import org.cr.pipeline.ui.components.ContactRow
import org.cr.pipeline.ui.components.DetailSection
import org.cr.pipeline.ui.components.DimmedOverlay
import org.cr.pipeline.ui.components.OverdueBanner
import org.cr.pipeline.ui.components.PlIconButton
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.PostingLinkRow
import org.cr.pipeline.ui.components.StatusChip
import org.cr.pipeline.ui.components.Timeline
import org.cr.pipeline.ui.components.TimelineEntry
import org.cr.pipeline.ui.components.contactInitials
import org.cr.pipeline.ui.components.changesLabel
import org.cr.pipeline.ui.components.rememberApplicationDetail
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

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
            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    DisplayText(detail.company, size = 30.sp, lineHeight = 32.sp)
                    BodyText(
                        detail.role,
                        size = 15.sp,
                        color = PlColors.fgSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
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
            val overdueReminder = detail.reminders.firstOrNull { it.overdue }
            if (overdueReminder != null) {
                OverdueBanner(
                    message = overdueReminder.message,
                    dueDate = overdueReminder.dueDate,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            if (detail.statusHistory.isNotEmpty()) {
                DetailSection(
                    label = "Status history",
                    right = { MonoText(changesLabel(detail.statusHistory.size), size = 9.sp, color = PlColors.fgMuted) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Timeline(entries = detail.statusHistory.map { TimelineEntry(it.status, it.date, it.note, it.current) })
                }
            }
            if (detail.contacts.isNotEmpty()) {
                DetailSection(
                    label = "Contacts",
                    right = { Icon(Icons.Filled.Add, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        detail.contacts.forEach { contact ->
                            ContactRow(ContactInfo(contact.name.contactInitials(), contact.name, contact.role, contact.email))
                        }
                    }
                }
            }
            DetailSection(
                label = "Notes",
                right = { Icon(Icons.Filled.Edit, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                BodyText(detail.notes, size = 13.5f.sp, color = PlColors.fgSecondary, lineHeight = 20.sp)
            }
            if (detail.postingUrl != null) {
                DetailSection(label = "Posting", modifier = Modifier.padding(horizontal = 16.dp)) {
                    PostingLinkRow(detail.postingUrl)
                    val sourceLine = listOfNotNull(
                        detail.source?.let { "Source: $it" },
                        detail.dateApplied?.let { "Added $it" },
                    ).joinToString(" · ")
                    if (sourceLine.isNotEmpty()) {
                        MonoText(sourceLine, size = 9.sp, color = PlColors.fgMuted)
                    }
                }
            }
        }
    }
}
