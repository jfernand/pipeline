/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pipeline.shared.generated.resources.Res
import pipeline.shared.generated.resources.barlow_condensed_extrabold
import pipeline.shared.generated.resources.ibm_plex_mono_medium
import pipeline.shared.generated.resources.ibm_plex_mono_semibold
import pipeline.shared.generated.resources.space_grotesk_medium
import pipeline.shared.generated.resources.space_grotesk_regular
import pipeline.shared.generated.resources.space_grotesk_semibold

/**
 * The design's Barlow Condensed / Space Grotesk / IBM Plex Mono stacks, bundled from
 * Google Fonts (OFL-licensed; see /fonts-licenses). Only the weights actually used by
 * [MonoText], [DisplayText] and [BodyText] are included.
 */
object PlType {
    @Composable
    fun display(): FontFamily {
        val extraBold = Font(Res.font.barlow_condensed_extrabold, FontWeight.ExtraBold)
        return remember(extraBold) { FontFamily(extraBold) }
    }

    @Composable
    fun body(): FontFamily {
        val regular = Font(Res.font.space_grotesk_regular, FontWeight.Normal)
        val medium = Font(Res.font.space_grotesk_medium, FontWeight.Medium)
        val semiBold = Font(Res.font.space_grotesk_semibold, FontWeight.SemiBold)
        return remember(regular, medium, semiBold) { FontFamily(regular, medium, semiBold) }
    }

    @Composable
    fun mono(): FontFamily {
        val medium = Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium)
        val semiBold = Font(Res.font.ibm_plex_mono_semibold, FontWeight.SemiBold)
        return remember(medium, semiBold) { FontFamily(medium, semiBold) }
    }
}

/** The design's ".label"/".mono" style: uppercase, wide tracking, monospace. [uppercase] defaults
 *  to true for the usual short-label case; set it false for text whose actual casing is data —
 *  e.g. a raw event payload — where transforming it would misrepresent what's stored. */
@Composable
fun MonoText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Medium,
    color: Color = PlColors.fgMuted,
    letterSpacing: TextUnit = 0.14f.em,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    uppercase: Boolean = true,
) {
    Text(
        text = if (uppercase) text.uppercase() else text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = PlType.mono(),
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
    color: Color = PlColors.fgPrimary,
    letterSpacing: TextUnit = (-0.01f).em,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = PlType.display(),
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
    color: Color = PlColors.fgPrimary,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = PlType.body(),
        fontWeight = weight,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
    )
}
