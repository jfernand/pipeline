package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.QrCodePlaceholder
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun PairingScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    Column(modifier.fillMaxSize().background(PlColors.bgBase)) {
        PlTopBar(title = "Pair a device", leftIcon = Icons.AutoMirrored.Filled.ArrowBack, onLeftClick = onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            BodyText(
                "Sync runs directly between your own devices over the local network. No account, no server, " +
                    "nothing leaves the two devices.",
                size = 13.5f.sp,
                color = PlColors.fgSecondary,
                lineHeight = 21.sp,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                    .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MonoText("Scan this on your other device", size = 9.5f.sp, color = PlColors.fgMuted)
                Column(Modifier.background(PlColors.paper, RoundedCornerShape(2.dp)).padding(12.dp)) {
                    QrCodePlaceholder()
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonoText("4K7P — 2QX9", size = 13.sp, color = PlColors.fgPrimary, letterSpacing = 0.2f.em)
                    MonoText("Code expires in 4:52", size = 9.sp, color = PlColors.fgMuted)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f).height(1.dp).background(PlColors.borderDefault)) {}
                MonoText("or", size = 9.sp, color = PlColors.fgMuted)
                Column(Modifier.weight(1f).height(1.dp).background(PlColors.borderDefault)) {}
            }
            PlSecondaryButton(
                "Scan a code instead",
                icon = Icons.Filled.QrCodeScanner,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Paired devices", count = 1)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                        .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.TabletMac, null, tint = PlColors.fgSecondary)
                    Column(Modifier.weight(1f)) {
                        BodyText("Pixel Tablet", size = 14.sp, weight = FontWeight.SemiBold, color = PlColors.fgPrimary)
                        MonoText(
                            "Last synced 11 min ago · 14 records",
                            size = 9.sp,
                            color = PlColors.fgMuted,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Dot(color = AppStatus.OFFER.color)
                }
            }
        }
    }
}
