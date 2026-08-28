/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import org.cr.pipeline.data.io.FilePicker
import org.cr.pipeline.data.io.createFilePicker
import org.koin.dsl.module

/** A single commonMain module, same reasoning as [fileArchiveModule]: [createFilePicker] is
 *  itself an expect/actual factory function, so no per-platform Koin wiring is needed here. */
val filePickerModule = module {
    single<FilePicker> { createFilePicker() }
}
