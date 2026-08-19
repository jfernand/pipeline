package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.cr.pipeline.data.JobApplicationRepository

/** Controls an in-process MCP server exposing tools to add/edit applications. Only the JVM
 *  (desktop) target actually runs a server — the MCP Kotlin SDK's server module has no
 *  Android/iOS variant, and js/wasmJs run in a browser sandbox that can't bind a listen socket
 *  regardless. Other platforms get a stub with [isSupported] false, mirroring how
 *  [org.cr.pipeline.data.createPreferencesSettings] varies per platform. */
interface McpServerController {
    val isSupported: Boolean

    fun observeStatus(): Flow<McpServerStatus>

    suspend fun start(port: Int)
    suspend fun stop()
}

sealed interface McpServerStatus {
    data object Stopped : McpServerStatus
    data class Running(val host: String, val port: Int) : McpServerStatus
    data class Error(val message: String) : McpServerStatus
}

/** [repository] is only used by the real (JVM) implementation, to back the add/edit-application
 *  tools — stub implementations on other platforms ignore it. */
expect fun createMcpServerController(
    repository: JobApplicationRepository,
    logger: Logger = Logger.withTag("McpServer"),
): McpServerController

/** Shared by every non-JVM platform actual — there's no per-platform state to hold since these
 *  targets never start a real server. */
object UnsupportedMcpServerController : McpServerController {
    override val isSupported: Boolean = false

    override fun observeStatus(): Flow<McpServerStatus> = flowOf(McpServerStatus.Stopped)

    override suspend fun start(port: Int) = Unit
    override suspend fun stop() = Unit
}
