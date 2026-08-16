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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.formatShort
import org.cr.pipeline.model.todayDate
import org.cr.pipeline.ui.components.Field
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.StatusPickerFlowRow
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawTopBorder

@Composable
fun StatusSheet(
    company: String,
    role: String,
    currentStatus: AppStatus,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSave: (AppStatus, String) -> Unit = { _, _ -> },
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var note by remember { mutableStateOf("") }

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
            DisplayText("Update status", size = 21.sp, letterSpacing = 0.02f.em)
            BodyText(
                "$company · $role",
                size = 13.sp,
                color = PlColors.fgMuted,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MonoText("New status", size = 9.5f.sp, color = PlColors.fgMuted)
            StatusPickerFlowRow(selected = selectedStatus, onSelect = { selectedStatus = it })
        }
        Field(
            label = "Note",
            tall = true,
            value = note,
            placeholder = "Anything worth remembering about this update.",
            onValueChange = { note = it },
        )
        Row(
            Modifier.fillMaxWidth().drawTopBorder(PlColors.borderSubtle).padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.CalendarToday, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
            MonoText(todayFormatted(), size = 10.sp, color = PlColors.fgSecondary, modifier = Modifier.weight(1f))
            MonoText("Dated today", size = 9.sp, color = PlColors.fgMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlSecondaryButton("Cancel", onClick = onCancel, height = 46.dp, modifier = Modifier.weight(1f))
            PlPrimaryButton(
                "Save update",
                onClick = { onSave(selectedStatus, note) },
                height = 46.dp,
                modifier = Modifier.weight(2f),
            )
        }
    }
}

private fun todayFormatted(): String {
    val today = todayDate()
    return "${today.formatShort()}, ${today.year}"
}
