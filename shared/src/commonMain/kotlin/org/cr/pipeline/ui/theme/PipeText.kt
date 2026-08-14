package org.cr.pipeline.ui.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Approximations of the web design's Barlow Condensed / Space Grotesk / IBM Plex Mono
 * stacks using platform-default font families, since the web font files aren't bundled
 * with the app.
 */
object PipeType {
    val display = FontFamily.SansSerif
    val body = FontFamily.SansSerif
    val mono = FontFamily.Monospace
}

/** The design's ".label"/".mono" style: uppercase, wide tracking, monospace. */
@Composable
fun MonoText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Medium,
    color: Color = PipeColors.fgMuted,
    letterSpacing: TextUnit = 0.14f.em,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = PipeType.mono,
        fontWeight = weight,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/** The design's display style: condensed, extra-bold, uppercase. */
@Composable
fun DisplayText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.ExtraBold,
    color: Color = PipeColors.fgPrimary,
    letterSpacing: TextUnit = (-0.01f).em,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = PipeType.display,
        fontWeight = weight,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
    )
}

/** The design's body style: regular sentence case copy. */
@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = PipeColors.fgPrimary,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = PipeType.body,
        fontWeight = weight,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
    )
}
