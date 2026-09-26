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

/** A status chip's filter state: unset (absent from the map), required ([POSITIVE] — show only
 *  this status), or excluded ([NEGATIVE] — show everything but this status). Purely a UI
 *  filtering concept, kept out of the event log entirely — nothing here is ever persisted. */
enum class StatusFilterMode { POSITIVE, NEGATIVE }

/**
 * Search text, status filter, and the applications list already split into "needs follow-up"
 * vs. everything else — the state every applications list screen needs, kept in one place so
 * search/filter behavior can't drift between layouts.
 */
@Composable
fun rememberApplicationListFilter(
    applications: List<JobApplication>,
    initialQuery: String = "",
    initialStatusFilters: Map<AppStatus, StatusFilterMode> = emptyMap(),
): ApplicationListFilterState {
    var query by remember { mutableStateOf(initialQuery) }
    var statusFilters by remember { mutableStateOf(initialStatusFilters) }
    val required = statusFilters.filterValues { it == StatusFilterMode.POSITIVE }.keys
    val excluded = statusFilters.filterValues { it == StatusFilterMode.NEGATIVE }.keys
    val visible = applications.filter {
        (required.isEmpty() || it.status in required) &&
            it.status !in excluded &&
            (query.isBlank() || it.company.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true))
    }
    return ApplicationListFilterState(
        query = query,
        onQueryChange = { query = it },
        statusFilters = statusFilters,
        onStatusFilterCycle = { status ->
            // not set -> required -> excluded -> not set.
            statusFilters = when (statusFilters[status]) {
                null -> statusFilters + (status to StatusFilterMode.POSITIVE)
                StatusFilterMode.POSITIVE -> statusFilters + (status to StatusFilterMode.NEGATIVE)
                StatusFilterMode.NEGATIVE -> statusFilters - status
            }
        },
        onClearStatusFilters = { statusFilters = emptyMap() },
        followUp = visible.filter { it.overdueDays != null },
        rest = visible.filter { it.overdueDays == null },
    )
}

/**
 * PL-041: reads a list deep link's `status` and `exclude` parameters — comma-separated status
 * names — into the same map the chips produce. A name matches an [AppStatus] by its label or its
 * enum name, ignoring case, spaces, hyphens and underscores, so `interviewing`, `interview`,
 * `phone-screen` and `Phone Screen` all work. Unknown names are dropped rather than failing the
 * link: a typo should still land on a usable list. A status named in both is excluded — the
 * narrower reading of a contradictory link.
 */
fun parseStatusFilters(status: String?, exclude: String?): Map<AppStatus, StatusFilterMode> {
    fun parse(names: String?) = names.orEmpty().split(',').mapNotNull(::statusByName).toSet()
    val excluded = parse(exclude)
    return (parse(status) - excluded).associateWith { StatusFilterMode.POSITIVE } +
        excluded.associateWith { StatusFilterMode.NEGATIVE }
}

private fun normalizeStatusName(name: String) = name.filter { it.isLetter() }.lowercase()

private fun statusByName(name: String): AppStatus? {
    val wanted = normalizeStatusName(name)
    if (wanted.isEmpty()) return null
    return AppStatus.entries.firstOrNull { normalizeStatusName(it.label) == wanted || normalizeStatusName(it.name) == wanted }
}

data class ApplicationListFilterState(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val statusFilters: Map<AppStatus, StatusFilterMode>,
    val onStatusFilterCycle: (AppStatus) -> Unit,
    val onClearStatusFilters: () -> Unit,
    val followUp: List<JobApplication>,
    val rest: List<JobApplication>,
)

/**
 * The "All · N" + one chip per [AppStatus] row content shown atop an applications list. Each
 * status chip cycles through [StatusFilterMode] independently on tap — not set, required
 * (positive), excluded (negative), back to not set — so, unlike a single-select filter, any
 * combination of requirements and exclusions can be active at once ("Interview or Offer, but
 * never Withdrawn" is `{INTERVIEW: POSITIVE, OFFER: POSITIVE, WITHDRAWN: NEGATIVE}`).
 *
 * Emits its chips as flat children — wrap it in a `Row(Modifier.horizontalScroll(...))` or a
 * `FlowRow` depending on how much width the caller has.
 */
@Composable
fun StatusFilterChips(totalCount: Int, filters: Map<AppStatus, StatusFilterMode>, onCycle: (AppStatus) -> Unit, onClear: () -> Unit) {
    PlFilterChip("All · $totalCount", active = filters.isEmpty(), onClick = onClear)
    AppStatus.entries.forEach { status ->
        val mode = filters[status]
        PlFilterChip(
            status.label,
            active = mode == StatusFilterMode.POSITIVE,
            negative = mode == StatusFilterMode.NEGATIVE,
            dotColor = if (mode == null) status.color else null,
            onClick = { onCycle(status) },
        )
    }
}
