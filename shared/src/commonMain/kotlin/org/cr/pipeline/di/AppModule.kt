package org.cr.pipeline.di

import org.cr.pipeline.data.JobApplicationRepository
import org.koin.dsl.module

val appModule = module {
    single { JobApplicationRepository() }
}
