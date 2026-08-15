package org.cr.pipeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.ui.screens.AddEditScreen
import org.cr.pipeline.ui.screens.StatusSheet
import org.cr.pipeline.ui.tablet.NavDestination
import org.cr.pipeline.ui.tablet.NavRail
import org.cr.pipeline.ui.tablet.TabletListContent
import org.cr.pipeline.ui.tablet.TabletSyncContent
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.koinInject

/** Entry point for the Pipeline tablet UI: a persistent nav rail plus a switchable content pane. */
@Composable
fun PipelineTabletApp() {
    val repository = koinInject<JobApplicationRepository>()
    val scope = rememberCoroutineScope()
    val applications by repository.observeApplications().collectAsState(initial = emptyList())
    var destination by remember { mutableStateOf(NavDestination.LIST) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var editingApplicationId by remember { mutableStateOf<Long?>(null) }
    var addEditOpen by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }
    val effectiveSelectedId = selectedId ?: applications.firstOrNull()?.id
    val selectedApplication = applications.firstOrNull { it.id == effectiveSelectedId }

    Row(Modifier.fillMaxSize().background(PlColors.bgBase).statusBarsPadding()) {
        NavRail(active = destination, onSelect = { destination = it })
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when {
                addEditOpen -> AddEditScreen(
                    applicationId = editingApplicationId,
                    onClose = {
                        editingApplicationId?.let { selectedId = it }
                        addEditOpen = false
                    },
                )
                destination == NavDestination.LIST -> TabletListContent(
                    applications = applications,
                    selectedId = effectiveSelectedId,
                    onSelect = { selectedId = it.id },
                    onNew = {
                        editingApplicationId = null
                        addEditOpen = true
                    },
                    onUpdateStatus = { sheetOpen = true },
                    onEdit = {
                        editingApplicationId = effectiveSelectedId
                        addEditOpen = true
                    },
                )
                destination == NavDestination.SYNC -> TabletSyncContent()
                else -> PlaceholderPane(destination.label)
            }
            if (sheetOpen && selectedApplication != null && !addEditOpen) {
                StatusSheet(
                    company = selectedApplication.company,
                    role = selectedApplication.role,
                    currentStatus = selectedApplication.status,
                    onCancel = { sheetOpen = false },
                    onSave = { status, note ->
                        scope.launch { repository.updateStatus(selectedApplication.id, status, note) }
                        sheetOpen = false
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 480.dp).padding(bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaceholderPane(label: String) {
    Box(Modifier.fillMaxSize().background(PlColors.bgBase), contentAlignment = Alignment.Center) {
        MonoText("$label — coming soon", size = 11.sp, color = PlColors.fgMuted)
    }
}
