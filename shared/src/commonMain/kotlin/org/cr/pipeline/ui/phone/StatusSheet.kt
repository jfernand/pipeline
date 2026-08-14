package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.formatShort
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.ui.components.PipeFilterChip
import org.cr.pipeline.ui.components.PipePrimaryButton
import org.cr.pipeline.ui.components.PipeSecondaryButton
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors
import org.cr.pipeline.ui.theme.drawTopBorder

@Composable
fun StatusSheet(
    company: String,
    role: String,
    currentStatus: AppStatus,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(PipeColors.bgRaised, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.size(width = 32.dp, height = 4.dp).background(PipeColors.borderStrong, RoundedCornerShape(2.dp)))
        }
        Column {
            DisplayText("Update status", size = 21.sp, letterSpacing = 0.02f.em)
            BodyText(
                "$company · $role",
                size = 13.sp,
                color = PipeColors.fgMuted,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MonoText("New status", size = 9.5f.sp, color = PipeColors.fgMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppStatus.entries.forEach { status ->
                    val isSelected = status == currentStatus
                    PipeFilterChip(status.label, active = isSelected, dotColor = if (isSelected) null else status.color)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MonoText("Note", size = 9.5f.sp, color = PipeColors.fgMuted)
            Box(
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .background(PipeColors.field, RoundedCornerShape(2.dp))
                    .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(2.dp))
                    .padding(12.dp),
            ) {
                BodyText("Anything worth remembering about this update.", size = 13.5f.sp, color = PipeColors.fgMuted)
            }
        }
        Row(
            Modifier.fillMaxWidth().drawTopBorder(PipeColors.borderSubtle).padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.CalendarToday, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp))
            MonoText(todayFormatted(), size = 10.sp, color = PipeColors.fgSecondary, modifier = Modifier.weight(1f))
            MonoText("Dated today", size = 9.sp, color = PipeColors.fgMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PipeSecondaryButton("Cancel", onClick = onClose, height = 46.dp, modifier = Modifier.weight(1f))
            PipePrimaryButton("Save update", onClick = onClose, height = 46.dp, modifier = Modifier.weight(2f))
        }
    }
}

private fun todayFormatted(): String {
    val today = todayDate()
    return "${today.formatShort()}, ${today.year}"
}
