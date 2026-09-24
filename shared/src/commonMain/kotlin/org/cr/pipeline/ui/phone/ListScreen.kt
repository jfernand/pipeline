/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.components.AppCard
import org.cr.pipeline.ui.components.DimmedOverlay
import org.cr.pipeline.ui.components.PlSearchField
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.components.StatusFilterChips
import org.cr.pipeline.ui.components.rememberApplicationListFilter
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun ListScreen(
    applications: List<JobApplication>,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onCard: (JobApplication) -> Unit = {},
    onSettings: () -> Unit = {},
    onDeleteApp: (JobApplication) -> Unit = {},
) {
    val filter = rememberApplicationListFilter(applications)

    DimmedOverlay(dimmed, modifier.background(PlColors.bgBase)) {
        Column(Modifier.fillMaxSize()) {
            PlTopBar(
                title = "Pipeline",
                leftIcon = Icons.Filled.Layers,
                rightActions = listOf(
                    Icons.Filled.Refresh to {},
                    Icons.Filled.Settings to onSettings,
                ),
            )
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PlSearchField(query = filter.query, onQueryChange = filter.onQueryChange)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusFilterChips(applications.size, filter.statusFilters, filter.onStatusFilterCycle, filter.onClearStatusFilters)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionLabel("Needs follow-up", count = filter.followUp.size, accent = true)
                filter.followUp.forEach { app -> SwipeToDeleteAppCard(app, onClick = { onCard(app) }, onDelete = { onDeleteApp(app) }) }
                Spacer(Modifier.height(6.dp))
                SectionLabel("All applications", count = filter.rest.size)
                filter.rest.forEach { app -> SwipeToDeleteAppCard(app, onClick = { onCard(app) }, onDelete = { onDeleteApp(app) }) }
            }
        }
    }
}

/** PL-024: swipe left to reveal a trash can, right up against the card's edge — a full swipe
 *  commits the delete directly, no separate confirmation. The gesture itself is deliberate enough
 *  friction; that's not true of a single tap, which is why the detail header's Delete icon goes
 *  through [org.cr.pipeline.ui.screens.DeleteConfirmSheet] instead. */
@Composable
private fun SwipeToDeleteAppCard(app: JobApplication, onClick: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            true
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(PlColors.danger, RoundedCornerShape(4.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = PlColors.onBrand)
            }
        },
    ) {
        AppCard(app, onClick = onClick)
    }
}
