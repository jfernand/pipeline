package org.cr.pipeline.di

import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.InMemoryApplicationStateStore
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.InMemoryEventLog
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single<ApplicationStateStore> { InMemoryApplicationStateStore() }
    single<EventLog> { InMemoryEventLog() }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
}
