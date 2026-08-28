/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import com.russhwolf.settings.Settings
import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.DemoSeedingEventLog
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
import org.cr.pipeline.data.showFakeData
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.EventLogKind
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single { buildDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().eventEnvelopeDao() }
    single<DeviceIdentityStore> { createDeviceIdentityStore() }
    single<Settings> { createPreferencesSettings() }
    // PL-019: which chain backs the app for this whole process is decided once, here — not
    // reactive to the preference changing later, since InMemoryApplicationStateStore only ever
    // materializes once. DemoSeedingEventLog only wraps the demo chain, never the real one.
    single<EventLog> {
        if (get<Settings>().showFakeData()) {
            DemoSeedingEventLog(RoomEventLog(get(), get(), EventLogKind.DEMO))
        } else {
            RoomEventLog(get(), get(), EventLogKind.REAL)
        }
    }
    single<ApplicationStateStore> { InMemoryApplicationStateStore(get()) }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get(), get()) }
    single<PreferencesStore> { SettingsPreferencesStore(get()) }
}
