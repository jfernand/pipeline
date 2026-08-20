/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val KEY_SYNC_NETWORK_MODE = "syncNetworkMode"
private const val KEY_DEVELOPER_MODE = "developerMode"
private const val KEY_MCP_SERVER_ENABLED = "mcpServerEnabled"
private const val KEY_MCP_SERVER_PORT = "mcpServerPort"

/**
 * The one [PreferencesStore] implementation, shared across every platform — [Settings]'s plain
 * key-value API is identical everywhere, unlike [ApplicationStateStore]/[EventLog] which needed
 * different implementations per storage shape. [Settings] itself has no change-notification
 * story on every platform (js/wasmJs's `StorageSettings` doesn't implement `ObservableSettings`),
 * so this keeps its own in-memory copy in sync on every write instead — valid because this store
 * is the only thing that ever mutates these keys.
 */
class SettingsPreferencesStore(private val settings: Settings) : PreferencesStore {
    private val state = MutableStateFlow(readPreferences())

    override fun observePreferences(): Flow<AppPreferences> = state

    override suspend fun setSyncNetworkMode(mode: SyncNetworkMode) {
        settings.putString(KEY_SYNC_NETWORK_MODE, mode.name)
        state.value = state.value.copy(syncNetworkMode = mode)
    }

    override suspend fun setDeveloperMode(enabled: Boolean) {
        settings.putBoolean(KEY_DEVELOPER_MODE, enabled)
        state.value = state.value.copy(developerMode = enabled)
    }

    override suspend fun setMcpServerEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_MCP_SERVER_ENABLED, enabled)
        state.value = state.value.copy(mcpServerEnabled = enabled)
    }

    override suspend fun setMcpServerPort(port: Int) {
        settings.putInt(KEY_MCP_SERVER_PORT, port)
        state.value = state.value.copy(mcpServerPort = port)
    }

    private fun readPreferences(): AppPreferences {
        val mode = settings.getStringOrNull(KEY_SYNC_NETWORK_MODE)
            ?.let { runCatching { SyncNetworkMode.valueOf(it) }.getOrNull() }
            ?: SyncNetworkMode.LOCAL_NETWORK_ONLY
        return AppPreferences(
            syncNetworkMode = mode,
            developerMode = settings.getBoolean(KEY_DEVELOPER_MODE, false),
            mcpServerEnabled = settings.getBoolean(KEY_MCP_SERVER_ENABLED, false),
        )
    }
}
