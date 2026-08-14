package org.cr.pipeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.sampleApplications
import org.cr.pipeline.ui.tablet.NavDestination
import org.cr.pipeline.ui.tablet.NavRail
import org.cr.pipeline.ui.tablet.TabletListContent
import org.cr.pipeline.ui.tablet.TabletSyncContent
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors

/** Entry point for the Pipeline tablet UI: a persistent nav rail plus a switchable content pane. */
@Composable
fun PipelineApp() {
    var destination by remember { mutableStateOf(NavDestination.LIST) }
    var selectedCompany by remember { mutableStateOf(sampleApplications.first().company) }

    Row(Modifier.fillMaxSize().background(PipeColors.bgBase)) {
        NavRail(active = destination, onSelect = { destination = it })
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (destination) {
                NavDestination.LIST -> TabletListContent(
                    applications = sampleApplications,
                    selectedCompany = selectedCompany,
                    onSelect = { selectedCompany = it.company },
                    onNew = {},
                )
                NavDestination.SYNC -> TabletSyncContent()
                NavDestination.FOLLOWUPS, NavDestination.SETTINGS -> PlaceholderPane(destination.label)
            }
        }
    }
}

@Composable
private fun PlaceholderPane(label: String) {
    Box(Modifier.fillMaxSize().background(PipeColors.bgBase), contentAlignment = Alignment.Center) {
        MonoText("$label — coming soon", size = 11.sp, color = PipeColors.fgMuted)
    }
}
