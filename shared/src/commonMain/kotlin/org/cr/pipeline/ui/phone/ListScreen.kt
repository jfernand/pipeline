package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.components.AppCard
import org.cr.pipeline.ui.components.DimmedOverlay
import org.cr.pipeline.ui.components.PipeFilterChip
import org.cr.pipeline.ui.components.PipeTopBar
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors

@Composable
fun ListScreen(
    applications: List<JobApplication>,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onCard: (JobApplication) -> Unit = {},
    onSettings: () -> Unit = {},
) {
    val followUp = applications.filter { it.overdueDays != null }
    val rest = applications.filter { it.overdueDays == null }

    DimmedOverlay(dimmed, modifier.background(PipeColors.bgBase)) {
        Column(Modifier.fillMaxSize()) {
            PipeTopBar(
                title = "Pipeline",
                leftIcon = Icons.Filled.Layers,
                rightActions = listOf(
                    Icons.Filled.Refresh to {},
                    Icons.Filled.Settings to onSettings,
                ),
            )
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(PipeColors.field, RoundedCornerShape(2.dp))
                        .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Search, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp))
                    MonoText("Search company or role", size = 10.5f.sp, color = PipeColors.fgMuted)
                }
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PipeFilterChip("All · ${applications.size}", active = true)
                    PipeFilterChip("Applied", dotColor = AppStatus.APPLIED.color)
                    PipeFilterChip("Phone screen", dotColor = AppStatus.SCREEN.color)
                    PipeFilterChip("Interviewing", dotColor = AppStatus.INTERVIEW.color)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionLabel("Needs follow-up", count = followUp.size, accent = true)
                followUp.forEach { app -> AppCard(app, onClick = { onCard(app) }) }
                Spacer(Modifier.height(6.dp))
                SectionLabel("All applications", count = rest.size)
                rest.forEach { app -> AppCard(app, onClick = { onCard(app) }) }
            }
        }
    }
}
