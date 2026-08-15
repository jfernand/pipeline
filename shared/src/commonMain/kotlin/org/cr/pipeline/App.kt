package org.cr.pipeline

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.cr.pipeline.di.platformDataModule
import org.cr.pipeline.ui.PipelineTabletApp
import org.cr.pipeline.ui.phone.PipelinePhoneApp
import org.cr.pipeline.ui.theme.PlColors
import org.koin.compose.KoinApplication
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
fun App(onNavHostReady: suspend (NavHostController) -> Unit = {}) {
    KoinApplication(koinConfiguration { modules(platformDataModule) }) {
        MaterialTheme(colorScheme = pipeDarkColorScheme) {
            val navController = rememberNavController()
            LaunchedEffect(navController) { onNavHostReady(navController) }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                if (maxWidth < COMPACT_WIDTH_BREAKPOINT) {
                    PipelinePhoneApp(navController)
                } else {
                    PipelineTabletApp(navController)
                }
            }
        }
    }
}
