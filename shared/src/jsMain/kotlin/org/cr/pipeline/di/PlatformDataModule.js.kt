/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import com.russhwolf.settings.Settings
import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.BrowserEventLog
import org.cr.pipeline.data.DeviceIdentityStore
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.InMemoryApplicationStateStore
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.SettingsPreferencesStore
import org.cr.pipeline.data.createDeviceIdentityStore
import org.cr.pipeline.data.createPreferencesSettings
import org.cr.pipeline.sync.event.EventLog
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    // Applications themselves still reset on reload (Room's KMP support doesn't cover js/wasmJs);
    // only the device identity, event log, and preferences persist, to localStorage.
    single<ApplicationStateStore> { InMemoryApplicationStateStore() }
    single<DeviceIdentityStore> { createDeviceIdentityStore() }
    single<EventLog> { BrowserEventLog(get()) }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
    single<Settings> { createPreferencesSettings() }
    single<PreferencesStore> { SettingsPreferencesStore(get()) }
}
