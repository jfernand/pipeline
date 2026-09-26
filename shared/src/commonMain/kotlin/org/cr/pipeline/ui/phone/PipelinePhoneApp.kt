/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.nav.DeepLinkBus
import org.cr.pipeline.ui.components.PlFab
import org.cr.pipeline.ui.components.parseStatusFilters
import org.cr.pipeline.ui.nav.AddEditRoute
import org.cr.pipeline.ui.nav.DetailRoute
import org.cr.pipeline.ui.nav.DetailSheet
import org.cr.pipeline.ui.nav.DevToolsRoute
import org.cr.pipeline.ui.nav.ListRoute
import org.cr.pipeline.ui.nav.PairRoute
import org.cr.pipeline.ui.nav.SettingsRoute
import org.cr.pipeline.ui.nav.SyncRoute
import org.cr.pipeline.ui.nav.toDetailSheet
import org.cr.pipeline.ui.screens.AddEditScreen
import org.cr.pipeline.ui.screens.ContactSheet
import org.cr.pipeline.ui.screens.DeleteConfirmSheet
import org.cr.pipeline.ui.screens.StatusSheet
import org.koin.compose.koinInject

/** Entry point for the Pipeline phone UI: single-pane routed navigation plus a status-update sheet. */
@Composable
fun PipelinePhoneApp(navController: NavHostController, modifier: Modifier = Modifier, initialDeepLink: String? = null) {
    val repository = koinInject<JobApplicationRepository>()
    val preferencesStore = koinInject<PreferencesStore>()
    val deepLinkBus = koinInject<DeepLinkBus>()
    val scope = rememberCoroutineScope()
    val applications by repository.observeApplications().collectAsState(initial = emptyList())
    var sheetApplicationId by remember { mutableStateOf<Long?>(null) }
    val sheetApplication = applications.firstOrNull { it.id == sheetApplicationId }
    var contactSheetApplicationId by remember { mutableStateOf<Long?>(null) }
    val contactSheetApplication = applications.firstOrNull { it.id == contactSheetApplicationId }
    // PL-024: only the detail header's single-tap Delete icon needs a confirm step — the list's
    // swipe-to-reveal gesture (ListScreen/AppCard) already has enough friction built in not to
    // need a second one, so it calls repository.deleteApplication directly.
    var deleteSheetApplicationId by remember { mutableStateOf<Long?>(null) }
    val deleteSheetApplication = applications.firstOrNull { it.id == deleteSheetApplicationId }
    val currentEntry by navController.currentBackStackEntryAsState()
    val onListRoute = currentEntry?.destination?.hasRoute<ListRoute>() == true
    val anySheetOpen = sheetApplication != null || contactSheetApplication != null || deleteSheetApplication != null

    Box(modifier.fillMaxSize().statusBarsPadding()) {
        NavHost(navController = navController, startDestination = ListRoute(), modifier = Modifier.fillMaxSize()) {
            composable<ListRoute>(
                deepLinks = listOf(
                    navDeepLink<ListRoute>(basePath = "pipeline://list"),
                    navDeepLink<ListRoute>(basePath = "https://pipeline.casaroja.es/list"),
                ),
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<ListRoute>()
                ListScreen(
                    applications = applications,
                    initialQuery = route.q.orEmpty(),
                    initialStatusFilters = parseStatusFilters(route.status, route.exclude),
                    dimmed = anySheetOpen,
                    onCard = { app -> navController.navigate(DetailRoute(app.id)) },
                    onSettings = { navController.navigate(SettingsRoute) },
                    onDeleteApp = { app -> scope.launch { repository.deleteApplication(app.id) } },
                )
            }
            composable<DetailRoute>(
                deepLinks = listOf(
                    navDeepLink<DetailRoute>(basePath = "pipeline://app"),
                    navDeepLink<DetailRoute>(basePath = "https://pipeline.casaroja.es/app"),
                ),
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<DetailRoute>()
                // See the matching comment in PipelineTabletApp.
                var sheetOpened by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    if (sheetOpened) return@LaunchedEffect
                    sheetOpened = true
                    when (route.sheet.toDetailSheet()) {
                        DetailSheet.STATUS -> sheetApplicationId = route.id
                        null -> Unit
                    }
                }
                DetailScreen(
                    applicationId = route.id,
                    dimmed = anySheetOpen,
                    onBack = { navController.popBackStack() },
                    onUpdate = { sheetApplicationId = route.id },
                    onEdit = { navController.navigate(AddEditRoute(route.id)) },
                    onAddContact = { contactSheetApplicationId = route.id },
                    onDelete = { deleteSheetApplicationId = route.id },
                )
            }
            // See the matching comment in PipelineTabletApp on why these are explicit patterns.
            composable<AddEditRoute>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "pipeline://app/new" },
                    navDeepLink { uriPattern = "https://pipeline.casaroja.es/app/new" },
                    navDeepLink { uriPattern = "pipeline://app/{id}/edit" },
                    navDeepLink { uriPattern = "https://pipeline.casaroja.es/app/{id}/edit" },
                ),
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<AddEditRoute>()
                AddEditScreen(applicationId = route.id, onClose = { navController.popBackStack() })
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onPair = { navController.navigate(PairRoute) },
                    onSync = { navController.navigate(SyncRoute) },
                    onDevTools = { navController.navigate(DevToolsRoute) },
                )
            }
            composable<PairRoute> {
                PairingScreen(onBack = { navController.popBackStack() })
            }
            composable<SyncRoute> {
                SyncScreen(onBack = { navController.popBackStack() })
            }
            composable<DevToolsRoute> {
                DevToolsScreen(onBack = { navController.popBackStack() })
            }
        }
        // See the matching comment in PipelineTabletApp: this must run in the same composition
        // pass as the NavHost above, not from App()'s onNavHostReady, or navController.graph may
        // not be set yet. PL-021: an explicit deep link wins over the last-viewed route below —
        // it reflects something that just happened (an OS intent, an agent's open_application
        // call), not where the user idly left off.
        LaunchedEffect(initialDeepLink) {
            if (initialDeepLink != null) {
                navController.handleDeepLink(NavDeepLinkRequest.Builder.fromUri(NavUri(initialDeepLink)).build())
            } else {
                preferencesStore.currentPreferences.lastDetailApplicationId?.let { id ->
                    navController.navigate(DetailRoute(id))
                }
            }
        }
        // Same graph-readiness requirement as the initialDeepLink effect above, but this one runs
        // for the composable's whole lifetime: the MCP server's open_application tool can push a
        // deep link at any point while the app is running, not just at cold start.
        LaunchedEffect(deepLinkBus) {
            deepLinkBus.deepLinks.collect { deepLink ->
                navController.handleDeepLink(NavDeepLinkRequest.Builder.fromUri(NavUri(deepLink)).build())
            }
        }
        // PL-021: only List/Detail landings are worth reopening into — Settings, Add/Edit, Sync,
        // Pair, and Dev Tools are all transient, so navigating through them leaves the last-saved
        // List/Detail screen untouched rather than clobbering it with somewhere not worth resuming.
        LaunchedEffect(currentEntry) {
            when {
                onListRoute -> preferencesStore.setLastDetailApplicationId(null)
                currentEntry?.destination?.hasRoute<DetailRoute>() == true ->
                    preferencesStore.setLastDetailApplicationId(currentEntry?.toRoute<DetailRoute>()?.id)
            }
        }
        if (onListRoute && !anySheetOpen) {
            PlFab(
                onClick = { navController.navigate(AddEditRoute()) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 20.dp),
            )
        }
        if (sheetApplication != null) {
            StatusSheet(
                company = sheetApplication.company,
                role = sheetApplication.role,
                currentStatus = sheetApplication.status,
                onCancel = { sheetApplicationId = null },
                onSave = { status, note ->
                    scope.launch { repository.updateStatus(sheetApplication.id, status, note) }
                    sheetApplicationId = null
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        if (contactSheetApplication != null) {
            ContactSheet(
                company = contactSheetApplication.company,
                role = contactSheetApplication.role,
                onCancel = { contactSheetApplicationId = null },
                onSave = { contact ->
                    scope.launch { repository.addContact(contactSheetApplication.id, contact) }
                    contactSheetApplicationId = null
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        if (deleteSheetApplication != null) {
            DeleteConfirmSheet(
                company = deleteSheetApplication.company,
                role = deleteSheetApplication.role,
                onCancel = { deleteSheetApplicationId = null },
                onConfirm = {
                    scope.launch { repository.deleteApplication(deleteSheetApplication.id) }
                    deleteSheetApplicationId = null
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
