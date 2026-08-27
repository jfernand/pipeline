/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow

/** App-level preferences, persisted per device — not synced between devices (unlike
 *  applications themselves), since each device may reasonably want its own sync/developer
 *  settings. */
interface PreferencesStore {
    fun observePreferences(): Flow<AppPreferences>
    suspend fun setSyncNetworkMode(mode: SyncNetworkMode)
    suspend fun setDeveloperMode(enabled: Boolean)

    /** Restart required to take effect — which physical [org.cr.pipeline.sync.event.EventLog]
     *  backs the app is decided once, when the DI graph is built (see `platformDataModule`). */
    suspend fun setShowFakeData(enabled: Boolean)
    suspend fun setMcpServerEnabled(enabled: Boolean)
    suspend fun setMcpServerPort(port: Int)
}
