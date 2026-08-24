/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.model.JobApplication
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.PlPrimaryButton
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.QrCodePlaceholder
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder

/**
 * The applications list, full width. A real tablet's width isn't reliably enough to show this
 * alongside a detail pane without squashing one of them, so selecting a card navigates to a
 * full-screen detail destination instead of showing it side by side (see TabletDetailScreen).
 */
@Composable
fun TabletListContent(
    applications: List<JobApplication>,
    selectedId: Long?,
    onSelect: (JobApplication) -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 1100.dp) 3 else 2
        ListPane(applications, selectedId, onSelect, onNew, modifier = Modifier.fillMaxSize(), columns = columns)
    }
}

/**
 * T3: pairing & sync log. Shared by phone and tablet (PL-003) — below [WIDE_BREAKPOINT] the QR
 * card and the paired-devices/log column stack vertically instead of sitting side by side, the
 * same threshold-and-stack pattern [org.cr.pipeline.ui.tablet.DetailPane]'s header already uses,
 * since the side-by-side layout's fixed-width QR column doesn't fit a phone-width screen.
 */
private val WIDE_BREAKPOINT = 640.dp

@Composable
fun TabletSyncContent(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize().background(PlColors.bgBase)) {
        val wide = maxWidth >= WIDE_BREAKPOINT
        Column(
            Modifier.padding(horizontal = if (wide) 40.dp else 20.dp, vertical = if (wide) 32.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column {
                DisplayText("Pair this device", size = if (wide) 31.sp else 26.sp)
                BodyText(
                    "Point your phone's camera at this code. Records copy directly between the two devices over the " +
                        "local network. No account, no server, nothing in between.",
                    size = 14.sp,
                    color = PlColors.fgSecondary,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 10.dp).widthIn(max = 560.dp),
                )
            }
            val qrCard = @Composable {
                Column(
                    Modifier
                        .then(if (wide) Modifier.width(300.dp) else Modifier.fillMaxWidth())
                        .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                        .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MonoText("Scan from your phone", size = 9.5f.sp, weight = FontWeight.SemiBold)
                    Column(
                        Modifier.background(PlColors.paper, RoundedCornerShape(2.dp)).padding(12.dp),
                    ) {
                        QrCodePlaceholder(qrSize = 200.dp)
                    }
                    MonoText("4K7P — 2QX9", size = 14.sp, color = PlColors.fgPrimary, letterSpacing = 0.2f.em)
                    MonoText("Code expires in 4:52", size = 9.sp)
                }
            }
            val details = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionLabel("Paired devices", count = 1)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                                .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Icon(Icons.Filled.Smartphone, null, tint = PlColors.fgSecondary, modifier = Modifier.size(20.dp))
                            Column(Modifier.weight(1f)) {
                                BodyText("Pixel 8", size = 14.5f.sp, weight = FontWeight.SemiBold, color = PlColors.fgPrimary)
                                MonoText(
                                    "Last synced 11 min ago · 14 records · 0 conflicts",
                                    size = 9.sp,
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                            Dot(color = AppStatus.OFFER.color)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionLabel("Sync log")
                        listOf(
                            "09:12" to "Pulled 3 records from Pixel 8",
                            "09:12" to "Pushed 1 status change",
                            "Yesterday" to "Full sync · 14 records",
                        ).forEach { (time, label) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .drawBottomBorder(PlColors.borderSubtle)
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                MonoText(time, size = 9.5f.sp, modifier = Modifier.width(80.dp))
                                BodyText(label, size = 13.5f.sp, color = PlColors.fgSecondary)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlSecondaryButton("Scan a code instead", icon = Icons.Filled.QrCodeScanner, height = 44.dp)
                        PlPrimaryButton("Sync now", height = 44.dp)
                    }
                }
            }
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    qrCard()
                    Column(Modifier.weight(1f)) { details() }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    qrCard()
                    details()
                }
            }
        }
    }
}
