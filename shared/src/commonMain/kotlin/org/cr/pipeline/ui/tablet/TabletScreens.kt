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

/** T1 (landscape) / T2 (portrait): adapts between list+detail and a two-column list. */
@Composable
fun TabletListContent(
    applications: List<JobApplication>,
    selectedId: Long?,
    onSelect: (JobApplication) -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            Row(Modifier.fillMaxSize()) {
                ListPane(applications, selectedId, onSelect, onNew, fixedWidth = 392.dp)
                DetailPane(applicationId = selectedId, modifier = Modifier.weight(1f))
            }
        } else {
            ListPane(
                applications,
                selectedId,
                onSelect,
                onNew,
                modifier = Modifier.fillMaxSize(),
                columns = 2,
                fixedWidth = null,
            )
        }
    }
}

/** T3: pairing & sync log. */
@Composable
fun TabletSyncContent(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(PlColors.bgBase)
            .padding(horizontal = 40.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            DisplayText("Pair this tablet", size = 31.sp)
            BodyText(
                "Point your phone's camera at this code. Records copy directly between the two devices over the " +
                    "local network. No account, no server, nothing in between.",
                size = 14.sp,
                color = PlColors.fgSecondary,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 10.dp).widthIn(max = 560.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(
                Modifier
                    .width(300.dp)
                    .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                    .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MonoText("Scan from your phone", size = 9.5f.sp, weight = FontWeight.SemiBold, color = PlColors.fgMuted)
                Column(
                    Modifier.background(PlColors.paper, RoundedCornerShape(2.dp)).padding(12.dp),
                ) {
                    QrCodePlaceholder(qrSize = 200.dp)
                }
                MonoText("4K7P — 2QX9", size = 14.sp, color = PlColors.fgPrimary, letterSpacing = 0.2f.em)
                MonoText("Code expires in 4:52", size = 9.sp, color = PlColors.fgMuted)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                color = PlColors.fgMuted,
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
                            MonoText(time, size = 9.5f.sp, color = PlColors.fgMuted, modifier = Modifier.width(80.dp))
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
    }
}
