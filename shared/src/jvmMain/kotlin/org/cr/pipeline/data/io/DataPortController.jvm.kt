/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.todayDate

actual fun createDataPortController(repository: JobApplicationRepository): DataPortController =
    JvmDataPortController(repository)

private val exportJson = Json { prettyPrint = true }
private val importJson = Json { ignoreUnknownKeys = true }

private class JvmDataPortController(
    private val repository: JobApplicationRepository,
) : DataPortController {
    override val isSupported: Boolean = true

    override suspend fun export(): DataPortResult {
        val file = withContext(Dispatchers.Swing) {
            showFileDialog(FileDialog.SAVE, "Export Pipeline data", "pipeline-export-${todayDate()}.json")
        } ?: return DataPortResult.Cancelled

        return try {
            val applications = repository.observeApplications().first()
            val inputs = applications.mapNotNull { repository.getApplicationInput(it.id) }
            val json = exportJson.encodeToString(ExportedApplications(applications = inputs))
            withContext(Dispatchers.IO) { file.writeText(json) }
            DataPortResult.Exported(inputs.size)
        } catch (e: Exception) {
            DataPortResult.Error(e.message ?: "Export failed")
        }
    }

    override suspend fun import(): DataPortResult {
        val file = withContext(Dispatchers.Swing) {
            showFileDialog(FileDialog.LOAD, "Import Pipeline data")
        } ?: return DataPortResult.Cancelled

        return try {
            val text = withContext(Dispatchers.IO) { file.readText() }
            val data = importJson.decodeFromString<ExportedApplications>(text)
            data.applications.forEach { repository.saveApplication(null, it) }
            DataPortResult.Imported(data.applications.size)
        } catch (e: Exception) {
            DataPortResult.Error(e.message ?: "Import failed")
        }
    }
}

/** [java.awt.FileDialog] over [javax.swing.JFileChooser]: it delegates to the OS-native picker on
 *  every desktop platform this app targets, and needs no look-and-feel setup. A null owner is
 *  fine — the dialog just isn't modal to a specific app window, which doesn't matter here since
 *  there's only ever one. Must run on the Swing/AWT event thread. */
private fun showFileDialog(mode: Int, title: String, defaultName: String? = null): File? {
    val dialog = FileDialog(null as Frame?, title, mode)
    defaultName?.let { dialog.file = it }
    if (mode == FileDialog.LOAD) dialog.setFilenameFilter { _, name -> name.endsWith(".json") }
    dialog.isVisible = true

    val directory = dialog.directory ?: return null
    val name = dialog.file ?: return null
    val target = File(directory, name)
    return if (mode == FileDialog.SAVE && target.extension != "json") File(directory, "$name.json") else target
}
