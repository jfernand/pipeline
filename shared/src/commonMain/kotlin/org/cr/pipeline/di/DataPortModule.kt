/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import org.cr.pipeline.data.io.DataPortController
import org.cr.pipeline.data.io.createDataPortController
import org.koin.dsl.module

/** A single commonMain module, same reasoning as [mcpDataModule]: [createDataPortController] is
 *  itself an expect/actual factory function, so no per-platform Koin wiring is needed here. */
val dataPortModule = module {
    single<DataPortController> { createDataPortController(get()) }
}
