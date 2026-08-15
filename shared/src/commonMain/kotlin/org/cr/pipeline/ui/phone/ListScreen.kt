package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.components.AppCard
import org.cr.pipeline.ui.components.DimmedOverlay
import org.cr.pipeline.ui.components.PlFilterChip
import org.cr.pipeline.ui.components.PlSearchField
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun ListScreen(
    applications: List<JobApplication>,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onCard: (JobApplication) -> Unit = {},
    onSettings: () -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<AppStatus?>(null) }
    val visible = applications.filter {
        (statusFilter == null || it.status == statusFilter) &&
            (query.isBlank() || it.company.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true))
    }
    val followUp = visible.filter { it.overdueDays != null }
    val rest = visible.filter { it.overdueDays == null }

    DimmedOverlay(dimmed, modifier.background(PlColors.bgBase)) {
        Column(Modifier.fillMaxSize()) {
            PlTopBar(
                title = "Pipeline",
                leftIcon = Icons.Filled.Layers,
                rightActions = listOf(
                    Icons.Filled.Refresh to {},
                    Icons.Filled.Settings to onSettings,
                ),
            )
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PlSearchField(query = query, onQueryChange = { query = it })
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PlFilterChip("All · ${applications.size}", active = statusFilter == null, onClick = { statusFilter = null })
                    AppStatus.entries.forEach { status ->
                        PlFilterChip(
                            status.label,
                            active = statusFilter == status,
                            dotColor = if (statusFilter == status) null else status.color,
                            onClick = { statusFilter = status },
                        )
                    }
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
