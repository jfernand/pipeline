/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.io.File
import org.cr.pipeline.data.db.AndroidDatabaseContext

/** Reuses the application [android.content.Context] `DatabaseBuilder.android.kt` already holds —
 *  no separate context wiring needed just for this. */
actual fun createFileArchiveService(): FileArchiveService =
    JavaZipFileArchiveService(File(AndroidDatabaseContext.applicationContext.filesDir, "pipeline-files.zip"))
