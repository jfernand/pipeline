package org.cr.pipeline

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cr.pipeline.ui.PipelineApp
import org.cr.pipeline.ui.theme.PipeColors

private val pipeDarkColorScheme = darkColorScheme(
    primary = PipeColors.brandPrimary,
    onPrimary = PipeColors.onBrand,
    background = PipeColors.bgBase,
    onBackground = PipeColors.fgPrimary,
    surface = PipeColors.bgBase,
    onSurface = PipeColors.fgPrimary,
)

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = pipeDarkColorScheme) {
        PipelineApp()
    }
}
