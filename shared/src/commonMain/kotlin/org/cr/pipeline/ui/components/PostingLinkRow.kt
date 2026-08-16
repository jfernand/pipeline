package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType

/** The bordered "job posting URL" row shown in a Posting DetailSection, on both layouts. */
@Composable
fun PostingLinkRow(url: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(PlColors.field, RoundedCornerShape(2.dp))
            .border(1.dp, PlColors.borderDefault, RoundedCornerShape(2.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Link, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
        Text(
            url,
            fontFamily = PlType.mono(),
            fontSize = 11.sp,
            color = PlColors.fgSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = PlColors.fgMuted, modifier = Modifier.size(14.dp))
    }
}
