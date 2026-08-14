package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors

data class TimelineEntry(val status: AppStatus, val date: String, val note: String, val current: Boolean = false)

val defaultTimeline = listOf(
    TimelineEntry(AppStatus.APPLIED, "Jun 3", "Referred by Dana W."),
    TimelineEntry(AppStatus.SCREEN, "Jun 10", "30 min with recruiter"),
    TimelineEntry(AppStatus.INTERVIEW, "Jun 20", "Round 1: Compose deep dive", current = true),
)

@Composable
fun Timeline(modifier: Modifier = Modifier, entries: List<TimelineEntry> = defaultTimeline) {
    Column(modifier) {
        entries.forEachIndexed { index, entry ->
            Row(Modifier.height(IntrinsicSize.Min)) {
                Column(Modifier.width(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(4.dp))
                    Dot(color = if (entry.current) PipeColors.brandPrimary else entry.status.color, size = if (entry.current) 10.dp else 7.dp)
                    if (index != entries.lastIndex) {
                        Spacer(Modifier.width(1.dp).weight(1f).background(PipeColors.borderDefault))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.padding(bottom = if (index == entries.lastIndex) 0.dp else 18.dp).widthIn(min = 0.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(entry.status, small = true)
                        MonoText(entry.date, size = 9.5f.sp, color = PipeColors.fgMuted)
                        if (entry.current) MonoText("· current", size = 9.sp, weight = FontWeight.SemiBold, color = PipeColors.brandPrimary)
                    }
                    BodyText(
                        entry.note,
                        size = 13.sp,
                        color = PipeColors.fgSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}
