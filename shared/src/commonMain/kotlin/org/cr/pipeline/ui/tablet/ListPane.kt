/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.components.AppCard
import org.cr.pipeline.ui.components.GridColumns
import org.cr.pipeline.ui.components.PlSearchField
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.components.StatusFilterChips
import org.cr.pipeline.ui.components.StatusFilterMode
import org.cr.pipeline.ui.components.rememberApplicationListFilter
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder

@Composable
fun ListPane(
    applications: List<JobApplication>,
    selectedId: Long?,
    onSelect: (JobApplication) -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 1,
    initialQuery: String = "",
    initialStatusFilters: Map<AppStatus, StatusFilterMode> = emptyMap(),
) {
    val filter = rememberApplicationListFilter(applications, initialQuery, initialStatusFilters)

    Box(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(PlColors.bgBase),
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .drawBottomBorder(PlColors.borderSubtle)
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DisplayText("Applications", size = 25.sp, letterSpacing = 0.02f.em)
                    MonoText("${applications.size} open", size = 10.sp)
                }
                PlSearchField(query = filter.query, onQueryChange = filter.onQueryChange)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusFilterChips(applications.size, filter.statusFilters, filter.onStatusFilterCycle, filter.onClearStatusFilters)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionLabel("Needs follow-up", count = filter.followUp.size, accent = true)
                GridColumns(filter.followUp, columns) { app ->
                    AppCard(app, selected = app.id == selectedId, onClick = { onSelect(app) })
                }
                Spacer(Modifier.height(6.dp))
                SectionLabel("All applications", count = filter.rest.size)
                GridColumns(filter.rest, columns) { app ->
                    AppCard(app, selected = app.id == selectedId, onClick = { onSelect(app) })
                }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height((2*96).dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, PlColors.bgBase))),
        )
        Surface(
            onClick = onNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .height(52.dp),
            color = PlColors.brandPrimary,
            shape = RoundedCornerShape(4.dp),
            shadowElevation = 6.dp,
        ) {
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.Add, null, tint = PlColors.onBrand, modifier = Modifier.size(20.dp))
                MonoText("New", size = 11.sp, weight = FontWeight.SemiBold, color = PlColors.onBrand)
            }
        }
    }
}
