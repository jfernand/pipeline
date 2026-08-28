/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.cr.pipeline.sync.event.EventProvenance
import org.cr.pipeline.ui.theme.PlColors

/**
 * PL-034: human (a device) or AI (an MCP client) — the small icon a list card shows, and the icon
 * half of the full identifier the detail view shows alongside it. Shared by [AppCard] and both
 * phone/tablet detail headers so the icon mapping can't drift between them the way three separate
 * private copies could.
 */
private fun provenanceIcon(provenance: EventProvenance): ImageVector? = when (provenance) {
    is EventProvenance.Device -> Icons.Filled.Person
    is EventProvenance.McpClient -> Icons.Filled.SmartToy
    EventProvenance.Unknown -> null
}

private fun provenanceDescription(provenance: EventProvenance): String = when (provenance) {
    is EventProvenance.Device -> "Last changed from a device"
    is EventProvenance.McpClient -> "Last changed by an AI agent"
    EventProvenance.Unknown -> ""
}

/** The detail view's "full identifier available" half of the summary — a full [EventProvenance.Device.deviceId]
 *  or [EventProvenance.McpClient.clientId], not the short icon+description a list card is limited
 *  to. Empty for [EventProvenance.Unknown] — nothing to identify for event payloads written before
 *  PL-033 gave events a provenance field at all. */
fun provenanceIdentifier(provenance: EventProvenance): String = when (provenance) {
    is EventProvenance.Device -> "Device ${provenance.deviceId.value}"
    is EventProvenance.McpClient -> "MCP client \"${provenance.clientId}\""
    EventProvenance.Unknown -> ""
}

/** Renders nothing for [EventProvenance.Unknown] — same reasoning as [provenanceIdentifier]. */
@Composable
fun ProvenanceIcon(provenance: EventProvenance, modifier: Modifier = Modifier, tint: Color = PlColors.fgMuted) {
    provenanceIcon(provenance)?.let { icon ->
        Icon(icon, contentDescription = provenanceDescription(provenance), tint = tint, modifier = modifier.size(14.dp))
    }
}
