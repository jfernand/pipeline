/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.awt.FileDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

actual fun createFilePicker(): FilePicker = JvmFilePicker

private object JvmFilePicker : FilePicker {
    override val isSupported: Boolean = true

    override suspend fun pickFile(): PickedFile? {
        val file = withContext(Dispatchers.Swing) { showFileDialog(FileDialog.LOAD, "Attach a file") } ?: return null
        return withContext(Dispatchers.IO) { PickedFile(file.name, file.readBytes()) }
    }
}
