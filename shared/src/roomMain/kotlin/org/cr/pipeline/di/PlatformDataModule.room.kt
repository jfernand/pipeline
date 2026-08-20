/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import com.russhwolf.settings.Settings
import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.DeviceIdentityStore
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.RoomApplicationStateStore
import org.cr.pipeline.data.RoomDeviceIdentityStore
import org.cr.pipeline.data.RoomEventLog
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
    single { get<AppDatabase>().applicationDao() }
    single { get<AppDatabase>().statusEventDao() }
    single { get<AppDatabase>().contactDao() }
    single { get<AppDatabase>().reminderDao() }
    single { get<AppDatabase>().deviceIdentityDao() }
    single { get<AppDatabase>().eventEnvelopeDao() }
    single<ApplicationStateStore> { RoomApplicationStateStore(get(), get(), get(), get()) }
    single<DeviceIdentityStore> { RoomDeviceIdentityStore(get()) }
    single<EventLog> { RoomEventLog(get(), get()) }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
    single<Settings> { createPreferencesSettings() }
    single<PreferencesStore> { SettingsPreferencesStore(get()) }
}
