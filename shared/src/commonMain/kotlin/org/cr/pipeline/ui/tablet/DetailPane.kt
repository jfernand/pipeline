package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.ApplicationDetail
import org.cr.pipeline.model.ReminderSummary
import org.cr.pipeline.ui.components.ContactsSection
import org.cr.pipeline.ui.components.DetailSection
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.OverdueBanner
import org.cr.pipeline.ui.components.PlIconButton
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PostingLinkRow
import org.cr.pipeline.ui.components.StatusChip
import org.cr.pipeline.ui.components.StatusHistorySection
import org.cr.pipeline.ui.components.rememberApplicationDetail
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.cr.pipeline.ui.theme.drawRightBorder

@Composable
fun DetailPane(
    applicationId: Long?,
    modifier: Modifier = Modifier,
    onUpdateStatus: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    val detail = rememberApplicationDetail(applicationId)

    Column(modifier.fillMaxHeight().fillMaxWidth().background(PlColors.bgBase)) {
        if (detail == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MonoText("Select an application", size = 11.sp, color = PlColors.fgMuted)
            }
            return@Column
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .drawBottomBorder(PlColors.borderDefault)
                .padding(start = 28.dp, top = 20.dp, end = 28.dp, bottom = 18.dp),
        ) {
            DetailHeader(detail, maxWidth, onUpdateStatus, onEdit)
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            DetailPrimaryColumn(
                detail,
                modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).drawRightBorder(PlColors.borderDefault),
            )
            DetailSecondaryColumn(
                detail,
                modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun DetailHeader(detail: ApplicationDetail, maxWidth: Dp, onUpdateStatus: () -> Unit, onEdit: () -> Unit) {
    val info: @Composable () -> Unit = {
        Column {
            DisplayText(detail.company, size = 39.sp, lineHeight = 39.sp)
            BodyText(detail.role, size = 16.sp, color = PlColors.fgSecondary, modifier = Modifier.padding(top = 6.dp))
            Row(
                Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusChip(detail.status)
                val activity = "${detail.daysSinceActivity}d since activity" +
                    (detail.source?.let { " · Source: $it" } ?: "")
                MonoText(activity, size = 9.5f.sp, color = PlColors.fgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    val actions: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (detail.postingUrl != null) {
                PlSecondaryButton("Posting", icon = Icons.AutoMirrored.Filled.OpenInNew, height = 44.dp)
            }
            PlIconButton(Icons.Filled.Edit, onClick = onEdit, size = 44.dp, bordered = true, tint = PlColors.fgSecondary)
            PlPrimaryButton("Update status", onClick = onUpdateStatus, height = 44.dp)
        }
    }
    // Below this, the info block (especially the company name at 39sp) doesn't have room
    // to share a row with the action buttons without getting crushed into a near-zero
    // width and wrapping character-by-character; stack them instead.
    if (maxWidth >= 640.dp) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.weight(1f)) { info() }
            actions()
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            info()
            actions()
        }
    }
}

@Composable
private fun DetailPrimaryColumn(detail: ApplicationDetail, modifier: Modifier = Modifier) {
    Column(modifier) {
        val overdueReminder = detail.reminders.firstOrNull { it.overdue }
        if (overdueReminder != null) {
            OverdueBanner(
                message = overdueReminder.message,
                dueDate = overdueReminder.dueDate,
                modifier = Modifier.padding(start = 24.dp, top = 18.dp, end = 24.dp),
            )
        }
        StatusHistorySection(detail.statusHistory, modifier = Modifier.padding(horizontal = 24.dp))
        ContactsSection(detail.contacts, modifier = Modifier.padding(horizontal = 24.dp))
    }
}

@Composable
private fun DetailSecondaryColumn(detail: ApplicationDetail, modifier: Modifier = Modifier) {
    Column(modifier) {
        DetailSection(
            label = "Notes",
            right = { Icon(Icons.Filled.Edit, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp)) },
        ) {
            BodyText(detail.notes, size = 13.5f.sp, color = PlColors.fgSecondary, lineHeight = 21.sp)
        }
        RemindersSection(detail.reminders)
        if (detail.postingUrl != null) {
            DetailSection(label = "Posting") {
                PostingLinkRow(detail.postingUrl)
            }
        }
    }
}

@Composable
private fun RemindersSection(reminders: List<ReminderSummary>) {
    if (reminders.isEmpty()) return
    DetailSection(
        label = "Reminders",
        right = { Icon(Icons.Filled.Add, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp)) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reminders.forEach { reminder -> ReminderRow(reminder) }
        }
    }
}

@Composable
private fun ReminderRow(reminder: ReminderSummary) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PlColors.bgRaised, RoundedCornerShape(2.dp))
            .border(
                1.dp,
                if (reminder.overdue) PlColors.overdueBorder else PlColors.borderDefault,
                RoundedCornerShape(2.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Dot(color = if (reminder.overdue) PlColors.brandPrimary else PlColors.fgMuted)
        BodyText(
            reminder.message,
            size = 13.5f.sp,
            color = PlColors.fgPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        MonoText(
            reminder.dueDate,
            size = 9.5f.sp,
            color = if (reminder.overdue) PlColors.brandPrimary else PlColors.fgMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
