package org.cr.pipeline.di

import org.cr.pipeline.data.JobApplicationRepository
import org.koin.core.module.Module

/**
 * Provides [JobApplicationRepository]: Room-backed on Android/JVM/iOS, in-memory on js/wasmJs
 * (Room's KMP support doesn't cover those targets).
 */
expect val platformDataModule: Module
