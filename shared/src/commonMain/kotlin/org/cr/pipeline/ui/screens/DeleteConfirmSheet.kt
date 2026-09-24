/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.PlColors

/** PL-024: a single tap on the Delete icon (detail header, either layout) is easy to trigger by
 *  accident, unlike the phone list's swipe-to-reveal gesture, which already has enough friction
 *  built into it not to need a second confirmation. Same bottom-sheet shape as [StatusSheet]/
 *  [ContactSheet] so a confirm dialog doesn't introduce a third visual pattern for "a sheet slides
 *  up from the bottom of this screen". */
@Composable
fun DeleteConfirmSheet(
    company: String,
    role: String,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(PlColors.bgRaised, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .border(1.dp, PlColors.borderDefault, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.size(width = 32.dp, height = 4.dp).background(PlColors.borderStrong, RoundedCornerShape(2.dp)))
        }
        Column {
            DisplayText("Delete application?", size = 21.sp, letterSpacing = 0.02f.em)
            BodyText(
                "$company · $role won't show up anywhere anymore. Its history stays in the event log.",
                size = 13.sp,
                color = PlColors.fgMuted,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlSecondaryButton("Cancel", onClick = onCancel, height = 46.dp, modifier = Modifier.weight(1f))
            PlPrimaryButton(
                "Delete",
                onClick = onConfirm,
                height = 46.dp,
                containerColor = PlColors.danger,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
