/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(
    val syncNetworkMode: SyncNetworkMode = SyncNetworkMode.LOCAL_NETWORK_ONLY,
    val developerMode: Boolean = false,
    val showFakeData: Boolean = false,
    val mcpServerEnabled: Boolean = false,
    val mcpServerAddress: String = "localhost",
    val mcpServerPort: Int = 34687,
    /** PL-021: null means the list was the last screen (or the app has never run) — the detail
     *  screen for one application is the only other screen worth reopening into. See
     *  [PreferencesStore.setLastDetailApplicationId]. */
    val lastDetailApplicationId: Long? = null,
)

@Serializable
enum class SyncNetworkMode(val label: String) {
    LOCAL_NETWORK_ONLY("Local network only"),
    ANY_NETWORK("Any network"),
}
