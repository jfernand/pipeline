/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.serialization.Serializable
import org.cr.pipeline.sync.chain.DeviceId

/**
 * Who or what made an [ApplicationEvent]. [Device] covers ordinary edits made through the app
 * itself, carrying that device's standardized [org.cr.pipeline.data.DeviceIdentityStore]-issued
 * id — the same [DeviceId] the event's envelope is stamped with, recorded here too because
 * provenance and envelope are otherwise decoded independently. [McpClient] covers changes made
 * through the MCP server (PL-013), identified by whatever token is available for the calling
 * AI/client. [Unknown] is the fallback for event payloads written before this field existed, so
 * reading old event logs always works.
 */
@Serializable
sealed interface EventProvenance {
    @Serializable
    data class Device(val deviceId: DeviceId) : EventProvenance

    @Serializable
    data class McpClient(val clientId: String) : EventProvenance

    @Serializable
    data object Unknown : EventProvenance
}
