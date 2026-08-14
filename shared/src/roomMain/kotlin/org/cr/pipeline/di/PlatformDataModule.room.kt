package org.cr.pipeline.di

import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.RoomJobApplicationRepository
import org.cr.pipeline.data.db.AppDatabase
import org.cr.pipeline.data.db.buildDatabase
import org.cr.pipeline.data.db.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single { buildDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().applicationDao() }
    single { get<AppDatabase>().statusEventDao() }
    single { get<AppDatabase>().contactDao() }
    single { get<AppDatabase>().reminderDao() }
    single<JobApplicationRepository> { RoomJobApplicationRepository(get(), get(), get(), get()) }
}
