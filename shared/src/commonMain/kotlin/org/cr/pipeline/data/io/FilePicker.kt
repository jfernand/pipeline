/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

/**
 * PL-020: prompts the user to choose an existing file from disk — PL-018's Add/Edit form uses
 * this for its "attach résumé/cover letter/file" buttons. [DataPortController]'s import/export
 * dialogs are a separate concern (a whole application collection, not one attachment) and keep
 * their own picker calls, but both share the underlying native dialog on JVM (see
 * `showFileDialog` in `jvmMain`).
 *
 * Only JVM (desktop) has a real implementation today; Android/iOS/js/wasmJs each get
 * [UnsupportedFilePicker], same rollout shape as [DataPortController]/[FileArchiveService].
 */
interface FilePicker {
    val isSupported: Boolean

    /** Prompts the user to pick one file to read. Null if cancelled or [isSupported] is false. */
    suspend fun pickFile(): PickedFile?
}

data class PickedFile(val fileName: String, val bytes: ByteArray)

/** [org.cr.pipeline.di.filePickerModule] resolves this once, on startup, the same way
 *  [createDataPortController] does for [DataPortController]. */
expect fun createFilePicker(): FilePicker

/** Shared by every platform without a real implementation yet. */
object UnsupportedFilePicker : FilePicker {
    override val isSupported: Boolean = false

    override suspend fun pickFile(): PickedFile? = null
}
