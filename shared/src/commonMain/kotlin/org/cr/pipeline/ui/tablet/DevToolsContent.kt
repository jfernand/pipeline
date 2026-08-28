/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.cr.pipeline.data.AppPreferences
import org.cr.pipeline.data.DeviceIdentityStore
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.io.FileArchiveEntry
import org.cr.pipeline.data.io.FileArchiveService
import org.cr.pipeline.sync.chain.EventEnvelope
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.ui.components.PlSecondaryButton
import org.cr.pipeline.ui.components.SectionLabel
import org.cr.pipeline.ui.components.SettingsRow
import org.cr.pipeline.ui.theme.BodyText
import org.cr.pipeline.ui.theme.DisplayText
import org.cr.pipeline.ui.theme.MonoText
import org.cr.pipeline.ui.theme.PlColors
import org.cr.pipeline.ui.theme.drawBottomBorder
import org.koin.compose.koinInject

/** Live internal state, not a mockup — this device's real identity and its real append-only
 *  event chain, straight from DeviceIdentityStore/EventLog. Only reachable when developer mode
 *  is on (see NavRail). */
@Composable
fun DevToolsContent(modifier: Modifier = Modifier) {
    val deviceIdentityStore = koinInject<DeviceIdentityStore>()
    val eventLog = koinInject<EventLog>()
    val preferencesStore = koinInject<PreferencesStore>()
    val repository = koinInject<JobApplicationRepository>()
    val fileService = koinInject<FileArchiveService>()
    val scope = rememberCoroutineScope()
    var deviceId by remember { mutableStateOf<String?>(null) }
    val chain by eventLog.observeChain().collectAsState(initial = emptyList())
    val preferences by preferencesStore.observePreferences().collectAsState(initial = AppPreferences())
    val applications by repository.observeApplications().collectAsState(initial = emptyList())
    var fileEntries by remember { mutableStateOf<List<FileArchiveEntry>>(emptyList()) }

    suspend fun refreshFileEntries() {
        fileEntries = fileService.listEntries()
    }

    LaunchedEffect(Unit) {
        deviceId = deviceIdentityStore.getDeviceId().value
        refreshFileEntries()
    }

    Column(
        modifier
            .fillMaxSize()
            .background(PlColors.bgBase)
            .padding(horizontal = 40.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            DisplayText("Developer tools", size = 31.sp)
            BodyText(
                "This device's identity and its append-only event chain, live.",
                size = 14.sp,
                color = PlColors.fgSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Sandbox")
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                    .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp)),
            ) {
                SettingsRow(
                    "Show fake data",
                    value = "A separate, seeded-once demo chain — your real data is untouched",
                    chevron = false,
                    trailingText = if (preferences.showFakeData) "ON" else "OFF",
                    trailingColor = if (preferences.showFakeData) PlColors.brandPrimary else PlColors.fgMuted,
                    onClick = { scope.launch { preferencesStore.setShowFakeData(!preferences.showFakeData) } },
                )
            }
            BodyText("Restart the app for this to take effect.", size = 12.sp, color = PlColors.fgMuted)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Device")
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                    .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                MonoText("DEVICE ID", size = 9.sp, modifier = Modifier.width(120.dp))
                MonoText(deviceId ?: "…", size = 12.sp, color = PlColors.fgPrimary)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Event log", count = chain.size)
            if (chain.isEmpty()) {
                BodyText("No events yet — make a change (create, edit, or update status) to see one here.", size = 13.sp, color = PlColors.fgMuted)
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                        .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp)),
                ) {
                    // chain is append order (oldest first, by sequence) — that's what the hash
                    // chain and diffChains rely on, so it's not something to reorder at the
                    // source. Reversed for display only, so the newest entry reads at the top.
                    chain.asReversed().forEach { envelope -> EventRow(envelope) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Files", count = fileEntries.size)
            if (!fileService.isSupported) {
                BodyText("Not available on this platform yet.", size = 13.sp, color = PlColors.fgMuted)
            } else {
                if (fileEntries.isEmpty()) {
                    BodyText("Nothing in the archive yet.", size = 13.sp, color = PlColors.fgMuted)
                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(PlColors.bgRaised, RoundedCornerShape(4.dp))
                            .border(1.dp, PlColors.borderDefault, RoundedCornerShape(4.dp)),
                    ) {
                        fileEntries.forEach { entry -> FileEntryRow(entry) }
                    }
                }
                PlSecondaryButton(
                    "Add test file",
                    onClick = {
                        val target = applications.firstOrNull() ?: return@PlSecondaryButton
                        scope.launch {
                            repository.attachFile(
                                id = target.id,
                                fileName = "dev-tools-test-${Clock.System.now().toEpochMilliseconds()}.txt",
                                bytes = "Dev Tools test attachment".encodeToByteArray(),
                            )
                            refreshFileEntries()
                        }
                    },
                    height = 40.dp,
                )
                if (applications.isEmpty()) {
                    BodyText(
                        "Add an application first — there's nothing to attach a test file to yet.",
                        size = 12.sp,
                        color = PlColors.fgMuted,
                    )
                }
            }
        }
    }
}

// Display-only. envelope.payload itself must stay exactly as EventEnvelopeCodec produced it —
// it's part of the hash input that gives the event its identity, so re-encoding it (even just to
// pretty-print) anywhere on the write path would change what the envelope hashes to.
private val prettyPrintJson = Json { prettyPrint = true }

private fun prettyPayload(payload: String): String =
    runCatching { prettyPrintJson.encodeToString(Json.parseToJsonElement(payload)) }.getOrDefault(payload)

@Composable
private fun EventRow(envelope: EventEnvelope) {
    Column(Modifier.fillMaxWidth().drawBottomBorder(PlColors.borderSubtle).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MonoText("#${envelope.sequence}", size = 9.sp, modifier = Modifier.width(36.dp))
            MonoText("hash ${envelope.hash.value.take(8)}", size = 9.sp, color = PlColors.fgPrimary)
            MonoText(
                if (envelope.parentHashes.isEmpty()) "genesis" else "parent ${envelope.parentHashes.joinToString(", ") { it.value.take(8) }}",
                size = 9.sp,
            )
            MonoText(
                Instant.fromEpochMilliseconds(envelope.timestampEpochMillis).toString(),
                size = 9.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        MonoText(
            remember(envelope.payload) { prettyPayload(envelope.payload) },
            size = 9.5f.sp,
            modifier = Modifier.padding(top = 6.dp),
            color = PlColors.fgSecondary,
            uppercase = false,
        )
    }
}

@Composable
private fun FileEntryRow(entry: FileArchiveEntry) {
    Row(
        Modifier.fillMaxWidth().drawBottomBorder(PlColors.borderSubtle).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MonoText(
            entry.path,
            size = 10.sp,
            color = PlColors.fgPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            uppercase = false,
        )
        MonoText(formatBytes(entry.sizeBytes), size = 10.sp)
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_024 * 1_024 -> "${oneDecimal(bytes / 1_024.0)} KB"
    else -> "${oneDecimal(bytes / (1_024.0 * 1_024.0))} MB"
}

// No java.util.Formatter/String.format in commonMain — plain integer math instead.
private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).toInt()
    return "${tenths / 10}.${tenths % 10}"
}
