/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.io

import java.awt.FileDialog
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
        val picked = withContext(Dispatchers.Swing) {
            showFileDialog(FileDialog.SAVE, "Export Pipeline data", "pipeline-export-${todayDate()}.json")
        } ?: return DataPortResult.Cancelled
        val file = if (picked.extension != "json") File(picked.parentFile, "${picked.name}.json") else picked

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
            showFileDialog(FileDialog.LOAD, "Import Pipeline data", filenameFilter = { name -> name.endsWith(".json") })
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
