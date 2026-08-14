package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.components.ContactInfo
import org.cr.pipeline.ui.components.ContactRow
import org.cr.pipeline.ui.components.DetailSection
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.PipeIconButton
import org.cr.pipeline.ui.components.PipePrimaryButton
import org.cr.pipeline.ui.components.PipeSecondaryButton
import org.cr.pipeline.ui.components.StatusChip
import org.cr.pipeline.ui.components.Timeline
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PipeColors
import org.cr.pipeline.ui.theme.PipeType
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.cr.pipeline.ui.theme.drawRightBorder

/**
 * Static detail content for the currently-selected application. The mock only designs a
 * single application's detail (Northwind Labs); selecting other cards in [ListPane] only
 * highlights them, matching the source design's behavior.
 */
@Composable
fun DetailPane(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxHeight().fillMaxWidth().background(PipeColors.bgBase)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, top = 20.dp, end = 28.dp, bottom = 18.dp)
                .drawBottomBorder(PipeColors.borderDefault),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(Modifier.weight(1f)) {
                DisplayText("Northwind Labs", size = 39.sp, lineHeight = 39.sp)
                BodyText(
                    "Staff Android Engineer",
                    size = 16.sp,
                    color = PipeColors.fgSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatusChip(AppStatus.INTERVIEW)
                    MonoText("2d since activity · Source: referral", size = 9.5f.sp, color = PipeColors.fgMuted)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PipeSecondaryButton("Posting", icon = Icons.AutoMirrored.Filled.OpenInNew, height = 44.dp)
                PipeIconButton(Icons.Filled.Edit, size = 44.dp, bordered = true, tint = PipeColors.fgSecondary)
                PipePrimaryButton("Update status", height = 44.dp)
            }
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .drawRightBorder(PipeColors.borderDefault),
            ) {
                Row(
                    Modifier
                        .padding(start = 24.dp, top = 18.dp, end = 24.dp)
                        .fillMaxWidth()
                        .background(PipeColors.overdueBg, RoundedCornerShape(4.dp))
                        .border(1.dp, PipeColors.overdueBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Notifications, null, tint = PipeColors.brandPrimary, modifier = Modifier.size(16.dp))
                    Column(Modifier.weight(1f)) {
                        MonoText("Follow-up overdue · 3d", size = 9.5f.sp, color = PipeColors.brandPrimary)
                        BodyText(
                            "Email Dana about round 2 timing",
                            size = 13.sp,
                            color = PipeColors.fgSecondary,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    MonoText("Jun 21", size = 9.5f.sp, color = PipeColors.fgMuted)
                }
                DetailSection(
                    label = "Status history",
                    right = { MonoText("3 changes", size = 9.sp, color = PipeColors.fgMuted) },
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    Timeline()
                }
                DetailSection(
                    label = "Contacts",
                    right = { Icon(Icons.Filled.Add, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ContactRow(ContactInfo("DW", "Dana Whitfield", "Engineering manager", "dana@northwindlabs.com"))
                        ContactRow(ContactInfo("MO", "Marcus Oyelaran", "Recruiter", "marcus@northwindlabs.com"))
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                DetailSection(
                    label = "Notes",
                    right = { Icon(Icons.Filled.Edit, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp)) },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BodyText(
                            "Round 2 is systems design. They run Compose across the whole app and asked how we handle " +
                                "multi-module builds. Comp band 185–205 plus equity. Dana said a decision lands within " +
                                "two weeks of the final round.",
                            size = 13.5f.sp,
                            color = PipeColors.fgSecondary,
                            lineHeight = 21.sp,
                        )
                        BodyText(
                            "Prep: draw the sync topology from memory. They asked twice about offline conflict " +
                                "handling, so it matters to them.",
                            size = 13.5f.sp,
                            color = PipeColors.fgSecondary,
                            lineHeight = 21.sp,
                        )
                    }
                }
                DetailSection(
                    label = "Reminders",
                    right = { Icon(Icons.Filled.Add, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp)) },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("Email Dana about round 2 timing", "Jun 21", true),
                            Triple("Systems design round", "Jun 27, 10:00", false),
                            Triple("Nudge if no reply", "Jul 1", false),
                        ).forEach { (label, date, overdue) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(PipeColors.bgRaised, RoundedCornerShape(2.dp))
                                    .border(1.dp, if (overdue) PipeColors.overdueBorder else PipeColors.borderDefault, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Dot(color = if (overdue) PipeColors.brandPrimary else PipeColors.fgMuted)
                                BodyText(label, size = 13.5f.sp, color = PipeColors.fgPrimary, modifier = Modifier.weight(1f))
                                MonoText(date, size = 9.5f.sp, color = if (overdue) PipeColors.brandPrimary else PipeColors.fgMuted)
                            }
                        }
                    }
                }
                DetailSection(label = "Posting") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(PipeColors.field, RoundedCornerShape(2.dp))
                            .border(1.dp, PipeColors.borderDefault, RoundedCornerShape(2.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.Link, null, tint = PipeColors.fgMuted, modifier = Modifier.size(16.dp))
                        Text(
                            "northwindlabs.com/careers/staff-android",
                            fontFamily = PipeType.mono,
                            fontSize = 11.sp,
                            color = PipeColors.fgSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = PipeColors.fgMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
