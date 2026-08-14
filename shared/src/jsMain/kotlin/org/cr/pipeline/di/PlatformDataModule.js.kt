package org.cr.pipeline.di

import org.cr.pipeline.data.InMemoryJobApplicationRepository
import org.cr.pipeline.data.JobApplicationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataModule: Module = module {
    single<JobApplicationRepository> { InMemoryJobApplicationRepository() }
}
