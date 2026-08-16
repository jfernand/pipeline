package org.cr.pipeline.di

import org.cr.pipeline.data.ApplicationStateStore
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.RoomApplicationStateStore
import org.cr.pipeline.data.db.AppDatabase
import org.cr.pipeline.data.db.buildDatabase
import org.cr.pipeline.data.db.getDatabaseBuilder
import org.cr.pipeline.sync.event.EventLog
import org.cr.pipeline.sync.event.InMemoryEventLog
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single { buildDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().applicationDao() }
    single { get<AppDatabase>().statusEventDao() }
    single { get<AppDatabase>().contactDao() }
    single { get<AppDatabase>().reminderDao() }
    single<ApplicationStateStore> { RoomApplicationStateStore(get(), get(), get(), get()) }
    // In-memory only for now: the event chain doesn't survive a restart yet, only the
    // materialized ApplicationStateStore does (Room, above).
    single<EventLog> { InMemoryEventLog() }
    single<JobApplicationRepository> { EventSourcedJobApplicationRepository(get(), get()) }
}
