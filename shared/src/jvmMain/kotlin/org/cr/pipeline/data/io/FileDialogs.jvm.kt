/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * PL-020: one file dialog, reused by [DataPortController]'s export/import (`DataPortController.jvm.kt`)
 * and by [FilePicker]'s attach-file affordance (`FilePicker.jvm.kt`, PL-018) — [java.awt.FileDialog]
 * over [javax.swing.JFileChooser]: it delegates to the OS-native picker on every desktop platform
 * this app targets, and needs no look-and-feel setup. A null owner is fine — the dialog just isn't
 * modal to a specific app window, which doesn't matter here since there's only ever one.
 *
 * Must run on the Swing/AWT event thread — callers are responsible for that (`Dispatchers.Swing`).
 * [filenameFilter], if given, only applies to `FileDialog.LOAD`; `SAVE` dialogs ignore it, the same
 * way [java.awt.FileDialog] itself does.
 */
internal fun showFileDialog(
    mode: Int,
    title: String,
    defaultName: String? = null,
    filenameFilter: ((name: String) -> Boolean)? = null,
): File? {
    val dialog = FileDialog(null as Frame?, title, mode)
    defaultName?.let { dialog.file = it }
    if (filenameFilter != null) dialog.setFilenameFilter { _, name -> filenameFilter(name) }
    dialog.isVisible = true

    val directory = dialog.directory ?: return null
    val name = dialog.file ?: return null
    return File(directory, name)
}
