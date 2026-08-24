/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * The date-entry equivalent of [Field] — same box, same label, but tapping it opens a real
 * calendar ([DatePickerDialog]) instead of accepting freehand text (PL-007-001). [clearable]
 * shows a small ✕ next to the calendar icon whenever [date] is set, since both of this field's
 * current callers (Date applied, Next action) are optional.
 *
 * [DatePickerState.selectedDateMillis] is defined as UTC midnight for the selected day, which is
 * exactly what [LocalDate.toEpochDays] already measures from — converting through epoch days
 * rather than any local-timezone millis avoids the classic date-picker off-by-one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select a date",
    clearable: Boolean = true,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MonoText(label, size = 9.5f.sp)
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 46.dp)
                .background(PlColors.field, RoundedCornerShape(2.dp))
                .border(1.dp, PlColors.borderDefault, RoundedCornerShape(2.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BodyText(
                date?.toString() ?: placeholder,
                size = 14.sp,
                color = if (date != null) PlColors.fgPrimary else PlColors.fgMuted,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f),
            )
            if (clearable && date != null) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Clear $label",
                    tint = PlColors.fgMuted,
                    modifier = Modifier.size(16.dp).clickable { onDateChange(null) },
                )
            }
            Icon(Icons.Filled.CalendarToday, null, tint = PlColors.fgMuted, modifier = Modifier.size(16.dp))
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date?.let { it.toEpochDays().toLong() * MILLIS_PER_DAY },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                PlPrimaryButton(
                    "OK",
                    height = 38.dp,
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onDateChange(LocalDate.fromEpochDays((millis / MILLIS_PER_DAY).toInt()))
                        }
                        showPicker = false
                    },
                )
            },
            dismissButton = {
                PlSecondaryButton("Cancel", height = 38.dp, onClick = { showPicker = false })
            },
        ) {
            DatePicker(state = state)
        }
    }
}
