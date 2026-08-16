package org.cr.pipeline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cr.pipeline.model.AppStatus

/** A wrapping row of every [AppStatus] as a selectable chip, for picking exactly one — the
 * add/edit form's "Status" field and the status-update sheet's "New status" field. */
@Composable
fun StatusPickerFlowRow(selected: AppStatus, onSelect: (AppStatus) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AppStatus.entries.forEach { status ->
            val isSelected = status == selected
            PlFilterChip(
                status.label,
                active = isSelected,
                dotColor = if (isSelected) null else status.color,
                onClick = { onSelect(status) },
            )
        }
    }
}
