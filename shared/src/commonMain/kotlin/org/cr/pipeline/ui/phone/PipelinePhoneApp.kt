package org.cr.pipeline.ui.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.ui.components.PlFab
import org.koin.compose.koinInject

private enum class PhoneScreen { LIST, DETAIL, ADD, SETTINGS, PAIR }

/** Entry point for the Pipeline phone UI: single-pane navigation plus a status-update sheet. */
@Composable
fun PipelinePhoneApp(modifier: Modifier = Modifier) {
    val repository = koinInject<JobApplicationRepository>()
    val applications by repository.observeApplications().collectAsState(initial = emptyList())
    var screen by remember { mutableStateOf(PhoneScreen.LIST) }
    var sheetOpen by remember { mutableStateOf(false) }
    var selectedApplicationId by remember { mutableStateOf<Long?>(null) }
    val selectedApplication = applications.firstOrNull { it.id == selectedApplicationId }

    Box(modifier.fillMaxSize()) {
        when (screen) {
            PhoneScreen.LIST -> ListScreen(
                applications = applications,
                dimmed = sheetOpen,
                onCard = { app ->
                    selectedApplicationId = app.id
                    screen = PhoneScreen.DETAIL
                },
                onSettings = { screen = PhoneScreen.SETTINGS },
            )
            PhoneScreen.DETAIL -> DetailScreen(
                applicationId = selectedApplicationId,
                dimmed = sheetOpen,
                onBack = { screen = PhoneScreen.LIST },
                onUpdate = { sheetOpen = true },
            )
            PhoneScreen.ADD -> AddEditScreen(onClose = { screen = PhoneScreen.LIST })
            PhoneScreen.SETTINGS -> SettingsScreen(
                onBack = { screen = PhoneScreen.LIST },
                onPair = { screen = PhoneScreen.PAIR },
            )
            PhoneScreen.PAIR -> PairingScreen(onBack = { screen = PhoneScreen.SETTINGS })
        }
        if (screen == PhoneScreen.LIST && !sheetOpen) {
            PlFab(
                onClick = { screen = PhoneScreen.ADD },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 20.dp),
            )
        }
        if (sheetOpen && selectedApplication != null) {
            StatusSheet(
                company = selectedApplication.company,
                role = selectedApplication.role,
                currentStatus = selectedApplication.status,
                onClose = { sheetOpen = false },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
