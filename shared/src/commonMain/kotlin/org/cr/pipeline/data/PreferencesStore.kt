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

    /** PL-021: a synchronous snapshot, for the one place a suspend [Flow] read can't work — a
     *  NavHost's launch-time redirect needs the last-viewed route before the first frame, not
     *  after a coroutine gets to run. Safe because the real implementation always has this ready
     *  before Compose can reach it: reading [com.russhwolf.settings.Settings] is itself
     *  synchronous, and happens at construction, not on first collection. */
    val currentPreferences: AppPreferences

    suspend fun setSyncNetworkMode(mode: SyncNetworkMode)
    suspend fun setDeveloperMode(enabled: Boolean)

    /** Restart required to take effect — which physical [org.cr.pipeline.sync.event.EventLog]
     *  backs the app is decided once, when the DI graph is built (see `platformDataModule`). */
    suspend fun setShowFakeData(enabled: Boolean)
    suspend fun setMcpServerEnabled(enabled: Boolean)
    suspend fun setMcpServerPort(port: Int)

    /** PL-021: [id] null means the list was the last screen; non-null is the application whose
     *  detail screen was. Called on every landing on either screen — not on the others (Settings,
     *  Add/Edit, ...) — so closing mid-Settings still resumes wherever the list/detail screen last
     *  was, rather than clobbering it with a screen that isn't worth reopening into. */
    suspend fun setLastDetailApplicationId(id: Long?)
}
