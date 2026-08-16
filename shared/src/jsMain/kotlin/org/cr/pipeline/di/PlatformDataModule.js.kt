package org.cr.pipeline.di

import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.BrowserDeviceIdentityStore
import org.cr.pipeline.data.BrowserEventLog
import org.cr.pipeline.data.DeviceIdentityStore
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.InMemoryApplicationStateStore
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.sync.event.EventLog
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    // Applications themselves still reset on reload (Room's KMP support doesn't cover js/wasmJs);
    // only the device identity and event log persist, to localStorage.
    single<ApplicationStateStore> { InMemoryApplicationStateStore() }
    single<DeviceIdentityStore> { BrowserDeviceIdentityStore() }
    single<EventLog> { BrowserEventLog(get()) }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
}
