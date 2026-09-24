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
 * PL-034: human (a device) or AI (an MCP client) — a small icon next to each status-history
 * entry (`Timeline`) showing who or what made *that* change. Not a whole-application summary:
 * an application's history can span both a device and an MCP client, so there's no single icon
 * that could stand for "the" application's provenance.
 */
private fun provenanceIcon(provenance: EventProvenance): ImageVector? = when (provenance) {
    is EventProvenance.Device -> Icons.Filled.Person
    is EventProvenance.McpClient -> Icons.Filled.SmartToy
    EventProvenance.Unknown -> null
}

private fun provenanceDescription(provenance: EventProvenance): String = when (provenance) {
    is EventProvenance.Device -> "Changed from a device"
    is EventProvenance.McpClient -> "Changed by an AI agent"
    EventProvenance.Unknown -> ""
}

/** Renders nothing for [EventProvenance.Unknown] — event payloads written before PL-033 gave
 *  events a provenance field at all have nothing to show here. */
@Composable
fun ProvenanceIcon(provenance: EventProvenance, modifier: Modifier = Modifier, tint: Color = PlColors.fgMuted) {
    provenanceIcon(provenance)?.let { icon ->
        Icon(icon, contentDescription = provenanceDescription(provenance), tint = tint, modifier = modifier.size(14.dp))
    }
}
