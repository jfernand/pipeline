package org.cr.pipeline.data.io

import kotlinx.serialization.Serializable
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationInput

/** Whole-collection export/import — everything a user would want backed up or moved between
 *  devices by hand, as a single JSON file the user picks the location for. Only JVM (desktop) has
 *  a real implementation today; Android/iOS/js/wasmJs each get their own real file-picker
 *  implementation later, same shape as [org.cr.pipeline.data.mcp.McpServerController]. */
interface DataPortController {
    val isSupported: Boolean

    /** Prompts the user for a save location, then writes every application there. */
    suspend fun export(): DataPortResult

    /** Prompts the user to pick a file, then adds every application it contains as a new entry
     *  (never overwrites an existing one — importing is always additive). */
    suspend fun import(): DataPortResult
}

sealed interface DataPortResult {
    data class Exported(val count: Int) : DataPortResult
    data class Imported(val count: Int) : DataPortResult
    data object Cancelled : DataPortResult
    data class Error(val message: String) : DataPortResult
}

/** The on-disk shape. [version] exists so a future format change can still read old exports. */
@Serializable
data class ExportedApplications(
    val version: Int = 1,
    val applications: List<ApplicationInput>,
)

/** [repository] is only used by real implementations — the shared stub ignores it. */
expect fun createDataPortController(repository: JobApplicationRepository): DataPortController

/** Shared by every platform without a real implementation yet. */
object UnsupportedDataPortController : DataPortController {
    override val isSupported: Boolean = false

    override suspend fun export(): DataPortResult = DataPortResult.Error("Not available on this platform yet")
    override suspend fun import(): DataPortResult = DataPortResult.Error("Not available on this platform yet")
}
