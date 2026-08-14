package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
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
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.PipeIconButton
import org.cr.pipeline.ui.components.PipePrimaryButton
import org.cr.pipeline.ui.components.PipeSecondaryButton
import org.cr.pipeline.ui.components.StatusChip
import org.cr.pipeline.ui.components.Timeline
import org.cr.pipeline.ui.components.TimelineEntry
import org.cr.pipeline.ui.components.contactInitials
import org.cr.pipeline.ui.components.changesLabel
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors
import org.cr.pipeline.ui.theme.PipeType
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.cr.pipeline.ui.theme.drawRightBorder
import org.koin.compose.koinInject

@Composable
fun DetailPane(applicationId: Long?, modifier: Modifier = Modifier, onUpdateStatus: () -> Unit = {}) {
    val repository = koinInject<JobApplicationRepository>()
    val detail by produceState<ApplicationDetail?>(initialValue = null, applicationId) {
        if (applicationId == null) {
            value = null
        } else {
            repository.observeApplicationDetail(applicationId).collect { value = it }
        }
    }

    Column(modifier.fillMaxHeight().fillMaxWidth().background(PipeColors.bgBase)) {
        val current = detail
        if (current == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MonoText("Select an application", size = 11.sp, color = PipeColors.fgMuted)
            }
            return@Column
        }
        Row(
            Modifier
                .fillMaxWidth()
                .drawBottomBorder(PipeColors.borderDefault)
                .padding(start = 28.dp, top = 20.dp, end = 28.dp, bottom = 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(Modifier.weight(1f)) {
                DisplayText(current.company, size = 39.sp, lineHeight = 39.sp)
                BodyText(
                    current.role,
                    size = 16.sp,
                    color = PipeColors.fgSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatusChip(current.status)
                    val activity = "${current.daysSinceActivity}d since activity" +
                        (current.source?.let { " · Source: $it" } ?: "")
                    MonoText(activity, size = 9.5f.sp, color = PipeColors.fgMuted)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (current.postingUrl != null) {
                    PipeSecondaryButton("Posting", icon = Icons.AutoMirrored.Filled.OpenInNew, height = 44.dp)
                }
                PipeIconButton(Icons.Filled.Edit, size = 44.dp, bordered = true, tint = PipeColors.fgSecondary)
                PipePrimaryButton("Update status", onClick = onUpdateStatus, height = 44.dp)
            }
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .drawRightBorder(PipeColors.borderDefault),
            ) {
                val overdueReminder = current.reminders.firstOrNull { it.overdue }
                if (overdueReminder != null) {
                    Row(
                        Modifier
                            .padding(start = 24.dp, top = 18.dp, end = 24.dp)
                            .fillMaxWidth()
                            .background(PipeColors.overdueBg, RoundedCornerShape(4.dp))
                            .border(1.dp, PipeColors.overdueBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.Notifications, null, tint = PipeColors.brandPrimary, modifier = Modifier.size(16.dp))
                        Column(Modifier.weight(1f)) {
                            MonoText("Follow-up overdue", size = 9.5f.sp, color = PipeColors.brandPrimary)
                            BodyText(
                                overdueReminder.message,
                                size = 13.sp,
                                color = PipeColors.fgSecondary,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        MonoText(overdueReminder.dueDate, size = 9.5f.sp, color = PipeColors.fgMuted)
                    }
                }
                if (current.statusHistory.isNotEmpty()) {
                    DetailSection(
                        label = "Status history",
                        right = { MonoText(changesLabel(current.statusHistory.size), size = 9.sp, color = PipeColors.fgMuted) },
                        modifier = Modifier.padding(horizontal = 24.dp),
                    ) {
                        Timeline(entries = current.statusHistory.map { TimelineEntry(it.status, it.date, it.note, it.current) })
                    }
                }
                if (current.contacts.isNotEmpty()) {
                    DetailSection(
                        label = "Contacts",
                        right = { Icon(Icons.Filled.Add, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.padding(horizontal = 24.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            current.contacts.forEach { contact ->
                                ContactRow(ContactInfo(contact.name.contactInitials(), contact.name, contact.role, contact.email))
                            }
                        }
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                DetailSection(
                    label = "Notes",
                    right = { Icon(Icons.Filled.Edit, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp)) },
                ) {
                    BodyText(
                        current.notes,
                        size = 13.5f.sp,
                        color = PipeColors.fgSecondary,
                        lineHeight = 21.sp,
                    )
                }
                if (current.reminders.isNotEmpty()) {
                    DetailSection(
                        label = "Reminders",
                        right = { Icon(Icons.Filled.Add, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp)) },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            current.reminders.forEach { reminder ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(PipeColors.bgRaised, RoundedCornerShape(2.dp))
                                        .border(
                                            1.dp,
                                            if (reminder.overdue) PipeColors.overdueBorder else PipeColors.borderDefault,
                                            RoundedCornerShape(2.dp),
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Dot(color = if (reminder.overdue) PipeColors.brandPrimary else PipeColors.fgMuted)
                                    BodyText(reminder.message, size = 13.5f.sp, color = PipeColors.fgPrimary, modifier = Modifier.weight(1f))
                                    MonoText(
                                        reminder.dueDate,
                                        size = 9.5f.sp,
                                        color = if (reminder.overdue) PipeColors.brandPrimary else PipeColors.fgMuted,
                                    )
                                }
                            }
                        }
                    }
                }
                if (current.postingUrl != null) {
                    DetailSection(label = "Posting") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(PipeColors.field, RoundedCornerShape(2.dp))
                                .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(2.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Filled.Link, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp))
                            Text(
                                current.postingUrl,
                                fontFamily = PipeType.mono(),
                                fontSize = 11.sp,
                                color = PipeColors.fgSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = PipeColors.fgMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
