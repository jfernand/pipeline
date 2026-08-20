/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(
    val syncNetworkMode: SyncNetworkMode = SyncNetworkMode.LOCAL_NETWORK_ONLY,
    val developerMode: Boolean = false,
    val mcpServerEnabled: Boolean = false,
    val mcpServerAddress: String = "localhost",
    val mcpServerPort: Int = 34687,
)

@Serializable
enum class SyncNetworkMode(val label: String) {
    LOCAL_NETWORK_ONLY("Local network only"),
    ANY_NETWORK("Any network"),
}
