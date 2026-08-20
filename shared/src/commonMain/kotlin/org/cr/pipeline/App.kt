/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.cr.pipeline.data.AppPreferences
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.mcp.McpServerController
import org.cr.pipeline.di.dataPortModule
import org.cr.pipeline.di.loggingModule
import org.cr.pipeline.di.mcpDataModule
import org.cr.pipeline.di.platformDataModule
import org.cr.pipeline.ui.PipelineTabletApp
import org.cr.pipeline.ui.phone.PipelinePhoneApp
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

private val pipeDarkColorScheme = darkColorScheme(
    primary = PlColors.brandPrimary,
    onPrimary = PlColors.onBrand,
    background = PlColors.bgBase,
    onBackground = PlColors.fgPrimary,
    surface = PlColors.bgBase,
    onSurface = PlColors.fgPrimary,
)

/** Material3's compact/medium width breakpoint: below this we're on a phone-class layout. */
private val COMPACT_WIDTH_BREAKPOINT = 600.dp

@Composable
@Preview
fun App(
    initialDeepLink: String? = null,
    onNavHostReady: suspend (NavHostController) -> Unit = {},
) {
    KoinApplication(koinConfiguration { modules(loggingModule, platformDataModule, mcpDataModule, dataPortModule) }) {
        MaterialTheme(colorScheme = pipeDarkColorScheme) {
            val navController = rememberNavController()
            val preferencesStore = koinInject<PreferencesStore>()
            val mcpServerController = koinInject<McpServerController>()
            val preferences by preferencesStore.observePreferences().collectAsState(initial = AppPreferences())
            LaunchedEffect(navController) { onNavHostReady(navController) }
            // Tied to this always-composed root (not the Settings screen, which can be
            // navigated away from) so the server's lifecycle matches the app's, not the screen's.
            LaunchedEffect(preferences.mcpServerEnabled) {
                if (mcpServerController.isSupported) {
                    if (preferences.mcpServerEnabled) mcpServerController.start(preferences.mcpServerPort) else mcpServerController.stop()
                }
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                if (maxWidth < COMPACT_WIDTH_BREAKPOINT) {
                    PipelinePhoneApp(navController, initialDeepLink = initialDeepLink)
                } else {
                    PipelineTabletApp(navController, initialDeepLink = initialDeepLink)
                }
            }
        }
    }
}
