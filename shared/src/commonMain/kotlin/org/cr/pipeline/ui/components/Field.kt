package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors

/** A labeled input-style box for the Add/Edit form. Read-only display; not a real text field. */
@Composable
fun Field(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    placeholder: String? = null,
    icon: ImageVector? = null,
    tall: Boolean = false,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MonoText(label, size = 9.5f.sp, color = PipeColors.fgMuted)
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (tall) 84.dp else 46.dp)
                .background(PipeColors.field, RoundedCornerShape(2.dp))
                .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(2.dp))
                .padding(horizontal = 12.dp, vertical = if (tall) 12.dp else 0.dp),
            verticalAlignment = if (tall) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BodyText(
                value ?: placeholder.orEmpty(),
                size = 14.sp,
                color = if (value != null) PipeColors.fgPrimary else PipeColors.fgMuted,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f),
            )
            if (icon != null) Icon(icon, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp))
        }
    }
}
