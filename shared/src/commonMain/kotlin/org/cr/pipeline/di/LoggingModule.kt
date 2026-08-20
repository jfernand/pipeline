/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.di

import co.touchlab.kermit.Logger
import org.koin.dsl.module

val loggingModule = module {
    single<Logger> { Logger }
}
