/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType

/**
 * A labeled input box matching the Add/Edit form's style. Read-only display when
 * [onValueChange] is null; a real (single- or multi-line) text field when it's provided.
 */
@Composable
fun Field(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    placeholder: String? = null,
    icon: ImageVector? = null,
    tall: Boolean = false,
    onValueChange: ((String) -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MonoText(label, size = 9.5f.sp)
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (tall) 84.dp else 46.dp)
                .background(PlColors.field, RoundedCornerShape(2.dp))
                .border(1.dp, PlColors.borderDefault, RoundedCornerShape(2.dp))
                .padding(horizontal = 12.dp, vertical = if (tall) 12.dp else 0.dp),
            verticalAlignment = if (tall) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onValueChange != null) {
                Box(Modifier.weight(1f)) {
                    if (value.isNullOrEmpty() && placeholder != null) {
                        BodyText(placeholder, size = 14.sp, color = PlColors.fgMuted, lineHeight = 21.sp)
                    }
                    BasicTextField(
                        value = value.orEmpty(),
                        onValueChange = onValueChange,
                        singleLine = !tall,
                        minLines = if (tall) 3 else 1,
                        textStyle = TextStyle(
                            fontFamily = PlType.body(),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = PlColors.fgPrimary,
                        ),
                        cursorBrush = SolidColor(PlColors.brandPrimary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                BodyText(
                    value ?: placeholder.orEmpty(),
                    size = 14.sp,
                    color = if (value != null) PlColors.fgPrimary else PlColors.fgMuted,
                    lineHeight = 21.sp,
                    modifier = Modifier.weight(1f),
                )
            }
            if (icon != null) Icon(icon, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
        }
    }
}
