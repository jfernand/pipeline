/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.cr.pipeline.data.AppPreferences
import org.cr.pipeline.data.DeviceIdentityStore
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.SyncNetworkMode
import org.cr.pipeline.data.io.DataPortController
import org.cr.pipeline.data.io.DataPortResult
import org.cr.pipeline.data.mcp.McpServerController
import org.cr.pipeline.data.mcp.McpServerStatus
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.ui.components.Dot
import org.cr.pipeline.ui.components.EditableAffordanceBox
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.PlTopBar
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.components.SettingsRow
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.PlType
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}, onPair: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val preferencesStore = koinInject<PreferencesStore>()
    val deviceIdentityStore = koinInject<DeviceIdentityStore>()
    val eventLog = koinInject<EventLog>()
    val mcpServerController = koinInject<McpServerController>()
    val dataPortController = koinInject<DataPortController>()
    val preferences by preferencesStore.observePreferences().collectAsState(initial = AppPreferences())
    val chain by eventLog.observeChain().collectAsState(initial = emptyList())
    val mcpStatus by mcpServerController.observeStatus().collectAsState(initial = McpServerStatus.Stopped)
    var deviceId by remember { mutableStateOf<String?>(null) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var importStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(preferences.developerMode) {
        if (preferences.developerMode && deviceId == null) {
            deviceId = deviceIdentityStore.getOrCreateDeviceId().value
        }
    }

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
        SettingsRow(
            "Sync over",
            value = preferences.syncNetworkMode.label,
            icon = Icons.Filled.Wifi,
            onClick = {
                val next = if (preferences.syncNetworkMode == SyncNetworkMode.LOCAL_NETWORK_ONLY) {
                    SyncNetworkMode.ANY_NETWORK
                } else {
                    SyncNetworkMode.LOCAL_NETWORK_ONLY
                }
                scope.launch { preferencesStore.setSyncNetworkMode(next) }
            },
        )
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("Data") }
        SettingsRow(
            "Export as JSON",
            value = exportStatus ?: "Everything, unencrypted, yours",
            icon = Icons.Filled.Download,
            chevron = false,
            onClick = { scope.launch { exportStatus = dataPortController.export().describe() } },
        )
        SettingsRow(
            "Import from file",
            value = importStatus,
            icon = Icons.Filled.Upload,
            chevron = false,
            onClick = { scope.launch { importStatus = dataPortController.import().describe() } },
        )
        SettingsRow("Follow-up reminders", value = "9:00, weekdays", icon = Icons.Filled.NotificationsActive)
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("About") }
        SettingsRow("Pipeline 1.4.0", value = "Build 2026.06.24", icon = Icons.Filled.Info, chevron = false)
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("Developer") }
        SettingsRow(
            "Developer mode",
            icon = Icons.Filled.Code,
            chevron = false,
            trailingText = if (preferences.developerMode) "ON" else "OFF",
            trailingColor = if (preferences.developerMode) PlColors.brandPrimary else PlColors.fgMuted,
            onClick = { scope.launch { preferencesStore.setDeveloperMode(!preferences.developerMode) } },
        )
        if (preferences.developerMode) {
            SettingsRow("Device ID", value = deviceId?.take(8) ?: "…", icon = Icons.Filled.Fingerprint, chevron = false)
            SettingsRow(
                "Event log",
                value = "${chain.size} event${if (chain.size == 1) "" else "s"}" +
                    (chain.lastOrNull()?.let { " · latest ${it.hash.value.take(8)}" } ?: ""),
                icon = Icons.Filled.Storage,
                chevron = false,
            )
        }
        if (mcpServerController.isSupported) {
            Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)) { SectionLabel("MCP server") }
            McpServerRow(
                enabled = preferences.mcpServerEnabled,
                port = preferences.mcpServerPort,
                status = mcpStatus,
                onToggle = { scope.launch { preferencesStore.setMcpServerEnabled(!preferences.mcpServerEnabled) } },
                onPortChange = { port -> scope.launch { preferencesStore.setMcpServerPort(port) } },
            )
        }
        Column(Modifier.padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 32.dp)) {
            MonoText("No account. No server. No telemetry.", size = 9.sp, color = PlColors.fgMuted)
            MonoText("Your data stays on your devices.", size = 9.sp, color = PlColors.fgMuted, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun DataPortResult.describe(): String? = when (this) {
    is DataPortResult.Exported -> "Exported $count application${if (count == 1) "" else "s"}"
    is DataPortResult.Imported -> "Imported $count application${if (count == 1) "" else "s"}"
    DataPortResult.Cancelled -> null
    is DataPortResult.Error -> "Error — $message"
}

/**
 * The "MCP server" toggle and its "Address" used to be two separate [SettingsRow]s — this merges
 * them into one, with the port editable in place (PL-013-001) instead of read-only. Tapping the
 * ON/OFF label still flips [enabled]; tapping the address/port line below it edits [port], same
 * commit-on-Return/Done/blur and Escape-to-cancel behavior as [org.cr.pipeline.ui.components.NotesSection].
 * A committed port change actually restarts the running server on the new port (see
 * [org.cr.pipeline.data.mcp.McpServerController.start]) rather than just relabeling the display.
 */
@Composable
private fun McpServerRow(
    enabled: Boolean,
    port: Int,
    status: McpServerStatus,
    onToggle: () -> Unit,
    onPortChange: (Int) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(port) { mutableStateOf(port.toString()) }
    var hasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun commit() {
        if (!editing) return
        editing = false
        val parsed = draft.toIntOrNull()
        if (parsed != null && parsed in 1..65535 && parsed != port) onPortChange(parsed)
    }

    fun cancel() {
        if (!editing) return
        editing = false
        draft = port.toString()
    }

    Row(
        Modifier
            .fillMaxWidth()
            .drawBottomBorder(PlColors.borderSubtle)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .defaultMinSize(minHeight = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.Dns, null, tint = PlColors.fgMuted, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            BodyText("MCP server", size = 14.5f.sp, color = PlColors.fgPrimary)
            // The corner-marked affordance sits on just the port digits, not the whole address —
            // "127.0.0.1:" and "/mcp" around it are fixed, not something tapping edits.
            Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!editing && enabled && status is McpServerStatus.Running) {
                    MonoText("${status.host}:", size = 9.sp, color = PlColors.fgMuted, uppercase = false)
                }
                EditableAffordanceBox(
                    editing = editing,
                    locked = !enabled,
                    onClick = if (editing || !enabled) null else { { editing = true; hasFocused = false } },
                ) {
                    if (editing) {
                        BasicTextField(
                            value = draft,
                            onValueChange = { draft = it.filter(Char::isDigit).take(5) },
                            textStyle = TextStyle(fontFamily = PlType.mono(), fontSize = 9.sp, color = PlColors.fgPrimary),
                            cursorBrush = SolidColor(PlColors.brandPrimary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { commit() }),
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onPreviewKeyEvent { keyEvent ->
                                    if (keyEvent.type != KeyEventType.KeyDown) {
                                        false
                                    } else if (keyEvent.key == Key.Escape) {
                                        cancel()
                                        true
                                    } else if (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) {
                                        commit()
                                        true
                                    } else {
                                        false
                                    }
                                }
                                .onFocusChanged { focus ->
                                    if (focus.isFocused) {
                                        hasFocused = true
                                    } else if (hasFocused) {
                                        commit()
                                    }
                                },
                        )
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    } else {
                        MonoText(port.toString(), size = 9.sp, color = PlColors.fgMuted, uppercase = false)
                    }
                }
                if (!editing) {
                    when {
                        enabled && status is McpServerStatus.Running ->
                            MonoText("/mcp", size = 9.sp, color = PlColors.fgMuted, uppercase = false)
                        enabled && status is McpServerStatus.Error ->
                            MonoText("  Error — ${status.message}", size = 9.sp, color = PlColors.fgMuted, uppercase = false)
                        enabled -> MonoText("  Starting…", size = 9.sp, color = PlColors.fgMuted, uppercase = false)
                    }
                }
            }
        }
        MonoText(
            if (enabled) "ON" else "OFF",
            size = 9.5f.sp,
            color = if (enabled) PlColors.brandPrimary else PlColors.fgMuted,
            modifier = Modifier.clickable(onClick = onToggle),
        )
    }
}
