/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import org.cr.pipeline.data.AppPreferences
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.nav.DeepLinkBus
import org.cr.pipeline.ui.nav.AddEditRoute
import org.cr.pipeline.ui.nav.DetailRoute
import org.cr.pipeline.ui.nav.DevToolsRoute
import org.cr.pipeline.ui.nav.FollowUpsRoute
import org.cr.pipeline.ui.nav.ListRoute
import org.cr.pipeline.ui.nav.PairRoute
import org.cr.pipeline.ui.nav.SettingsRoute
import org.cr.pipeline.ui.nav.SyncRoute
import org.cr.pipeline.ui.phone.PairingScreen
import org.cr.pipeline.ui.phone.SettingsScreen
import org.cr.pipeline.ui.screens.AddEditScreen
import org.cr.pipeline.ui.screens.StatusSheet
import org.cr.pipeline.ui.tablet.DevToolsContent
import org.cr.pipeline.ui.tablet.NavDestination
import org.cr.pipeline.ui.tablet.NavRail
import org.cr.pipeline.ui.tablet.TabletDetailScreen
import org.cr.pipeline.ui.tablet.TabletListContent
import org.cr.pipeline.ui.tablet.TabletSyncContent
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.koinInject

/** Entry point for the Pipeline tablet UI: a persistent nav rail plus a routed content area. */
@Composable
fun PipelineTabletApp(navController: NavHostController, initialDeepLink: String? = null) {
    val repository = koinInject<JobApplicationRepository>()
    val preferencesStore = koinInject<PreferencesStore>()
    val deepLinkBus = koinInject<DeepLinkBus>()
    val scope = rememberCoroutineScope()
    val applications by repository.observeApplications().collectAsState(initial = emptyList())
    val preferences by preferencesStore.observePreferences().collectAsState(initial = AppPreferences())
    var activeRailDestination by remember { mutableStateOf(NavDestination.LIST) }
    var lastViewedId by remember { mutableStateOf<Long?>(null) }
    var statusSheetApplicationId by remember { mutableStateOf<Long?>(null) }
    val statusSheetApplication = applications.firstOrNull { it.id == statusSheetApplicationId }

    Row(Modifier.fillMaxSize().background(PlColors.bgBase).statusBarsPadding()) {
        NavRail(
            active = activeRailDestination,
            showDevTools = preferences.developerMode,
            onSelect = { destination ->
                activeRailDestination = destination
                val route = when (destination) {
                    NavDestination.LIST -> ListRoute
                    NavDestination.FOLLOWUPS -> FollowUpsRoute
                    NavDestination.SYNC -> SyncRoute
                    NavDestination.SETTINGS -> SettingsRoute
                    NavDestination.DEVTOOLS -> DevToolsRoute
                }
                navController.navigate(route) {
                    popUpTo(ListRoute)
                    launchSingleTop = true
                }
            },
        )
        Box(Modifier.weight(1f).fillMaxHeight()) {
            NavHost(navController = navController, startDestination = ListRoute, modifier = Modifier.fillMaxSize()) {
                composable<ListRoute> {
                    TabletListContent(
                        applications = applications,
                        selectedId = lastViewedId,
                        onSelect = { app ->
                            lastViewedId = app.id
                            navController.navigate(DetailRoute(app.id))
                        },
                        onNew = { navController.navigate(AddEditRoute()) },
                    )
                }
                composable<DetailRoute>(
                    deepLinks = listOf(
                        navDeepLink<DetailRoute>(basePath = "pipeline://app"),
                        navDeepLink<DetailRoute>(basePath = "https://pipeline.casaroja.es/app"),
                    ),
                ) { backStackEntry ->
                    val route = backStackEntry.toRoute<DetailRoute>()
                    TabletDetailScreen(
                        applicationId = route.id,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(AddEditRoute(route.id)) },
                        onUpdateStatus = { statusSheetApplicationId = route.id },
                    )
                }
                composable<AddEditRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<AddEditRoute>()
                    AddEditScreen(applicationId = route.id, onClose = { navController.popBackStack() })
                }
                composable<SyncRoute> { TabletSyncContent() }
                composable<FollowUpsRoute> { PlaceholderPane("Follow-ups") }
                composable<SettingsRoute> {
                    SettingsScreen(onBack = { navController.popBackStack() }, onPair = { navController.navigate(PairRoute) })
                }
                composable<PairRoute> { PairingScreen(onBack = { navController.popBackStack() }) }
                composable<DevToolsRoute> { DevToolsContent() }
            }
            // Runs in the same composition pass as the NavHost above (both are children of this
            // Box), so navController.graph is guaranteed to already be set here — unlike calling
            // handleDeepLink from App()'s onNavHostReady, which fires from an ancestor before this
            // width-gated subtree (and therefore NavHost) has necessarily been composed at all.
            LaunchedEffect(initialDeepLink) {
                if (initialDeepLink != null) {
                    navController.handleDeepLink(NavDeepLinkRequest.Builder.fromUri(NavUri(initialDeepLink)).build())
                }
            }
            // Same graph-readiness requirement as the initialDeepLink effect above, but this one
            // runs for the composable's whole lifetime: the MCP server's open_application tool can
            // push a deep link at any point while the app is running, not just at cold start.
            LaunchedEffect(deepLinkBus) {
                deepLinkBus.deepLinks.collect { deepLink ->
                    navController.handleDeepLink(NavDeepLinkRequest.Builder.fromUri(NavUri(deepLink)).build())
                }
            }
            if (statusSheetApplication != null) {
                StatusSheet(
                    company = statusSheetApplication.company,
                    role = statusSheetApplication.role,
                    currentStatus = statusSheetApplication.status,
                    onCancel = { statusSheetApplicationId = null },
                    onSave = { status, note ->
                        scope.launch { repository.updateStatus(statusSheetApplication.id, status, note) }
                        statusSheetApplicationId = null
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
