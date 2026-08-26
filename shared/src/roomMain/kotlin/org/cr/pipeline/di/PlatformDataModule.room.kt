/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import com.russhwolf.settings.Settings
import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.DeviceIdentityStore
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.InMemoryApplicationStateStore
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.RoomEventLog
import org.cr.pipeline.data.createDeviceIdentityStore
import org.cr.pipeline.data.SettingsPreferencesStore
import org.cr.pipeline.data.createPreferencesSettings
import org.cr.pipeline.data.db.AppDatabase
import org.cr.pipeline.data.db.buildDatabase
import org.cr.pipeline.data.db.getDatabaseBuilder
import org.cr.pipeline.sync.event.EventLog
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single { buildDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().eventEnvelopeDao() }
    single<DeviceIdentityStore> { createDeviceIdentityStore() }
    single<EventLog> { RoomEventLog(get(), get()) }
    single<ApplicationStateStore> { InMemoryApplicationStateStore(get()) }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
    single<Settings> { createPreferencesSettings() }
    single<PreferencesStore> { SettingsPreferencesStore(get()) }
}
