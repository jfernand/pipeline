package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.components.AppCard
import org.cr.pipeline.ui.components.GridColumns
import org.cr.pipeline.ui.components.PipeFilterChip
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.cr.pipeline.ui.theme.drawRightBorder

@Composable
fun ListPane(
    applications: List<JobApplication>,
    selectedCompany: String?,
    onSelect: (JobApplication) -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 1,
    fixedWidth: Dp? = 392.dp,
) {
    val followUp = applications.filter { it.overdueDays != null }
    val rest = applications.filter { it.overdueDays == null }

    Box(
        modifier
            .then(if (fixedWidth != null) Modifier.width(fixedWidth) else Modifier.fillMaxWidth())
            .fillMaxHeight()
            .drawRightBorder(PipeColors.borderDefault)
            .background(PipeColors.bgBase),
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 12.dp)
                    .drawBottomBorder(PipeColors.borderSubtle),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DisplayText("Applications", size = 25.sp, letterSpacing = 0.02f.em)
                    MonoText("${applications.size} open", size = 10.sp, color = PipeColors.fgMuted)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(PipeColors.field, RoundedCornerShape(2.dp))
                        .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Search, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp))
                    MonoText("Search company or role", size = 10.5f.sp, color = PipeColors.fgMuted)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PipeFilterChip("All · ${applications.size}", active = true)
                    PipeFilterChip("Applied", dotColor = AppStatus.APPLIED.color)
                    PipeFilterChip("Phone screen", dotColor = AppStatus.SCREEN.color)
                    PipeFilterChip("Interviewing", dotColor = AppStatus.INTERVIEW.color)
                    PipeFilterChip("Offer", dotColor = AppStatus.OFFER.color)
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
                SectionLabel("Needs follow-up", count = followUp.size, accent = true)
                GridColumns(followUp, columns) { app ->
                    AppCard(app, selected = app.company == selectedCompany, onClick = { onSelect(app) })
                }
                Spacer(Modifier.height(6.dp))
                SectionLabel("All applications", count = rest.size)
                GridColumns(rest, columns) { app ->
                    AppCard(app, selected = app.company == selectedCompany, onClick = { onSelect(app) })
                }
            }
        }
        Surface(
            onClick = onNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .height(52.dp),
            color = PipeColors.brandPrimary,
            shape = RoundedCornerShape(4.dp),
            shadowElevation = 6.dp,
        ) {
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.Add, null, tint = PipeColors.onBrand, modifier = Modifier.size(20.dp))
                MonoText("New", size = 11.sp, weight = FontWeight.SemiBold, color = PipeColors.onBrand)
            }
        }
    }
}
