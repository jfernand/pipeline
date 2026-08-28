/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.io.File

/** Same `~/.pipeline/` directory `DatabaseBuilder.jvm.kt` already uses for `pipeline.db`. */
actual fun createFileArchiveService(): FileArchiveService =
    JavaZipFileArchiveService(File(System.getProperty("user.home"), ".pipeline/pipeline-files.zip"))
