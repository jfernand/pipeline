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
    // Applications persist the same way every platform's do now: BrowserEventLog durably writes
    // every event to localStorage, and InMemoryApplicationStateStore rebuilds the current state
    // from it on first access — nothing here is Room's KMP support finally covering js/wasmJs,
    // this platform never needed Room, just a store that replays the (already-persisted) log.
    single<DeviceIdentityStore> { createDeviceIdentityStore() }
    single<EventLog> { BrowserEventLog(get()) }
    single<ApplicationStateStore> { InMemoryApplicationStateStore(get()) }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
    single<Settings> { createPreferencesSettings() }
    single<PreferencesStore> { SettingsPreferencesStore(get()) }
}
