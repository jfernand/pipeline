package org.cr.pipeline

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cr.pipeline.ui.PipelineTabletApp
import org.cr.pipeline.ui.phone.PipelinePhoneApp
import org.cr.pipeline.ui.theme.PipeColors

private val pipeDarkColorScheme = darkColorScheme(
    primary = PipeColors.brandPrimary,
    onPrimary = PipeColors.onBrand,
    background = PipeColors.bgBase,
    onBackground = PipeColors.fgPrimary,
    surface = PipeColors.bgBase,
    onSurface = PipeColors.fgPrimary,
)

/** Material3's compact/medium width breakpoint: below this we're on a phone-class layout. */
private val COMPACT_WIDTH_BREAKPOINT = 600.dp

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = pipeDarkColorScheme) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth < COMPACT_WIDTH_BREAKPOINT) {
                PipelinePhoneApp()
            } else {
                PipelineTabletApp()
            }
        }
    }
}
