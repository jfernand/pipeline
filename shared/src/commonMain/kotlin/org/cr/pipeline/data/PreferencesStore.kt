package org.cr.pipeline.data

import kotlinx.coroutines.flow.Flow

/** App-level preferences, persisted per device — not synced between devices (unlike
 *  applications themselves), since each device may reasonably want its own sync/developer
 *  settings. */
interface PreferencesStore {
    fun observePreferences(): Flow<AppPreferences>
    suspend fun setSyncNetworkMode(mode: SyncNetworkMode)
    suspend fun setDeveloperMode(enabled: Boolean)
    suspend fun setMcpServerEnabled(enabled: Boolean)
}
