package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.ui.components.ContactInfo
import org.cr.pipeline.ui.components.ContactRow
import org.cr.pipeline.ui.components.DetailSection
import org.cr.pipeline.ui.components.DimmedOverlay
import org.cr.pipeline.ui.components.PlIconButton
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.StatusChip
import org.cr.pipeline.ui.components.Timeline
import org.cr.pipeline.ui.components.TimelineEntry
import org.cr.pipeline.ui.components.contactInitials
import org.cr.pipeline.ui.components.changesLabel
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType
import org.koin.compose.koinInject

@Composable
fun DetailScreen(
    applicationId: Long?,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onBack: () -> Unit = {},
    onUpdate: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    val repository = koinInject<JobApplicationRepository>()
    val detail by produceState<ApplicationDetail?>(initialValue = null, applicationId) {
        if (applicationId == null) {
            value = null
        } else {
            repository.observeApplicationDetail(applicationId).collect { value = it }
        }
    }

    DimmedOverlay(dimmed, modifier.background(PlColors.bgBase)) {
        val current = detail
        if (current == null) {
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
                    DisplayText(current.company, size = 30.sp, lineHeight = 32.sp)
                    BodyText(
                        current.role,
                        size = 15.sp,
                        color = PlColors.fgSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(current.status)
                    MonoText("${current.daysSinceActivity}d since activity", size = 9.5f.sp, color = PlColors.fgMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlPrimaryButton("Update status", onClick = onUpdate, height = 46.dp, modifier = Modifier.weight(1f))
                    PlIconButton(Icons.AutoMirrored.Filled.OpenInNew, size = 46.dp, bordered = true, tint = PlColors.fgPrimary, iconSize = 18.dp)
                }
            }
            val overdueReminder = current.reminders.firstOrNull { it.overdue }
            if (overdueReminder != null) {
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
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
                        BodyText(
                            overdueReminder.message,
                            size = 13.sp,
                            color = PlColors.fgSecondary,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    MonoText(overdueReminder.dueDate, size = 9.5f.sp, color = PlColors.fgMuted)
                }
            }
            Spacer(Modifier.height(18.dp))
            if (current.statusHistory.isNotEmpty()) {
                DetailSection(
                    label = "Status history",
                    right = { MonoText(changesLabel(current.statusHistory.size), size = 9.sp, color = PlColors.fgMuted) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Timeline(entries = current.statusHistory.map { TimelineEntry(it.status, it.date, it.note, it.current) })
                }
            }
            if (current.contacts.isNotEmpty()) {
                DetailSection(
                    label = "Contacts",
                    right = { Icon(Icons.Filled.Add, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        current.contacts.forEach { contact ->
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
                BodyText(current.notes, size = 13.5f.sp, color = PlColors.fgSecondary, lineHeight = 20.sp)
            }
            if (current.postingUrl != null) {
                DetailSection(label = "Posting", modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(PlColors.field, RoundedCornerShape(2.dp))
                            .border(1.dp, PlColors.borderDefault, RoundedCornerShape(2.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.Link, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
                        Text(
                            current.postingUrl,
                            fontFamily = PlType.mono(),
                            fontSize = 11.sp,
                            color = PlColors.fgSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = PlColors.fgMuted, modifier = Modifier.size(14.dp))
                    }
                    val sourceLine = listOfNotNull(
                        current.source?.let { "Source: $it" },
                        current.dateApplied?.let { "Added $it" },
                    ).joinToString(" · ")
                    if (sourceLine.isNotEmpty()) {
                        MonoText(sourceLine, size = 9.sp, color = PlColors.fgMuted)
                    }
                }
            }
        }
    }
}
