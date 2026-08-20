/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication

/**
 * Search text, status filter, and the applications list already split into "needs follow-up"
 * vs. everything else — the state every applications list screen needs, kept in one place so
 * search/filter behavior can't drift between layouts.
 */
@Composable
fun rememberApplicationListFilter(applications: List<JobApplication>): ApplicationListFilterState {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<AppStatus?>(null) }
    val visible = applications.filter {
        (statusFilter == null || it.status == statusFilter) &&
            (query.isBlank() || it.company.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true))
    }
    return ApplicationListFilterState(
        query = query,
        onQueryChange = { query = it },
        statusFilter = statusFilter,
        onStatusFilterChange = { statusFilter = it },
        followUp = visible.filter { it.overdueDays != null },
        rest = visible.filter { it.overdueDays == null },
    )
}

data class ApplicationListFilterState(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val statusFilter: AppStatus?,
    val onStatusFilterChange: (AppStatus?) -> Unit,
    val followUp: List<JobApplication>,
    val rest: List<JobApplication>,
)

/**
 * The "All · N" + one chip per [AppStatus] row content shown atop an applications list.
 * Emits its chips as flat children — wrap it in a `Row(Modifier.horizontalScroll(...))` or a
 * `FlowRow` depending on how much width the caller has.
 */
@Composable
fun StatusFilterChips(totalCount: Int, selected: AppStatus?, onSelect: (AppStatus?) -> Unit) {
    PlFilterChip("All · $totalCount", active = selected == null, onClick = { onSelect(null) })
    AppStatus.entries.forEach { status ->
        PlFilterChip(
            status.label,
            active = selected == status,
            dotColor = if (selected == status) null else status.color,
            onClick = { onSelect(status) },
        )
    }
}
