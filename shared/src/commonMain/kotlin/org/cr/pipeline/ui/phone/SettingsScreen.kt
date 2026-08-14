package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.components.SettingsRow
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}, onPair: () -> Unit = {}) {
    Column(modifier.fillMaxSize().background(PlColors.bgBase).verticalScroll(rememberScrollState())) {
        PlTopBar(title = "Settings", leftIcon = Icons.AutoMirrored.Filled.ArrowBack, onLeftClick = onBack)
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                    .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Dot(color = AppStatus.OFFER.color)
                    MonoText("Sync up to date", size = 9.5f.sp, color = PlColors.fgSecondary, modifier = Modifier.weight(1f))
                    MonoText("11 min ago", size = 9.sp, color = PlColors.fgMuted)
                }
                BodyText("Pixel Tablet · 14 records, 0 conflicts", size = 13.sp, color = PlColors.fgMuted)
                PlSecondaryButton("Sync now", height = 42.dp, modifier = Modifier.fillMaxWidth())
            }
        }
        Column(Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("Sync") }
        SettingsRow("Pair a device", value = "1 device paired", icon = Icons.Filled.QrCode, onClick = onPair)
        SettingsRow("Sync over", value = "Local network only", icon = Icons.Filled.Wifi)
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("Data") }
        SettingsRow("Export as JSON", value = "Everything, unencrypted, yours", icon = Icons.Filled.Download)
        SettingsRow("Import from file", icon = Icons.Filled.Upload)
        SettingsRow("Follow-up reminders", value = "9:00, weekdays", icon = Icons.Filled.NotificationsActive)
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("About") }
        SettingsRow("Pipeline 1.4.0", value = "Build 2026.06.24", icon = Icons.Filled.Info, chevron = false)
        Column(Modifier.padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 32.dp)) {
            MonoText("No account. No server. No telemetry.", size = 9.sp, color = PlColors.fgMuted)
            MonoText("Your data stays on your devices.", size = 9.sp, color = PlColors.fgMuted, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
