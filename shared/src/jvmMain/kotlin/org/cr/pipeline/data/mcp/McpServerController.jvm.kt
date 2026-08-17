package org.cr.pipeline.data.mcp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cr.pipeline.data.JobApplicationRepository
import org.http4k.server.Http4kServer
import org.http4k.server.asServer

actual fun createMcpServerController(repository: JobApplicationRepository): McpServerController =
    JvmMcpServerController(repository)

private const val LOOPBACK_HOST = "127.0.0.1"

private class JvmMcpServerController(
    private val repository: JobApplicationRepository,
) : McpServerController {
    override val isSupported: Boolean = true

    private val _status = MutableStateFlow<McpServerStatus>(McpServerStatus.Stopped)
    private var server: Http4kServer? = null

    override fun observeStatus(): Flow<McpServerStatus> = _status.asStateFlow()

    override suspend fun start() {
        if (server != null) return
        try {
            val started = buildMcpApp(repository).asServer(LoopbackSunHttp(0)).start()
            server = started
            _status.value = McpServerStatus.Running(LOOPBACK_HOST, started.port())
        } catch (e: Exception) {
            _status.value = McpServerStatus.Error(e.message ?: "Failed to start MCP server")
        }
    }

    override suspend fun stop() {
        server?.stop()
        server = null
        _status.value = McpServerStatus.Stopped
    }
}
