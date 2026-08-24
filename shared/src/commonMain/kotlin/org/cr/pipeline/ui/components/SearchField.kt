/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType

/**
 * Read-only display when [onQueryChange] is null (matches the original mock); a real text
 * field, filtering as you type, when it's provided.
 */
@Composable
fun PlSearchField(
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChange: ((String) -> Unit)? = null,
    placeholder: String = "Search company or role",
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(PlColors.field, RoundedCornerShape(2.dp))
            .border(1.dp, PlColors.borderDefault, RoundedCornerShape(2.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
        if (onQueryChange != null) {
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    MonoText(placeholder, size = 10.5f.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = PlType.mono(),
                        fontSize = 10.5f.sp,
                        letterSpacing = 0.14f.em,
                        color = PlColors.fgPrimary,
                    ),
                    cursorBrush = SolidColor(PlColors.brandPrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            MonoText(placeholder, size = 10.5f.sp)
        }
    }
}
