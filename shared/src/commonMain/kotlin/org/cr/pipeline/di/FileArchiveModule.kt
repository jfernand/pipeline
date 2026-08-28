/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import org.cr.pipeline.data.io.FileArchiveService
import org.cr.pipeline.data.io.createFileArchiveService
import org.koin.dsl.module

/** A single commonMain module, same reasoning as [dataPortModule]: [createFileArchiveService] is
 *  itself an expect/actual factory function, so no per-platform Koin wiring is needed here. */
val fileArchiveModule = module {
    single<FileArchiveService> { createFileArchiveService() }
}
