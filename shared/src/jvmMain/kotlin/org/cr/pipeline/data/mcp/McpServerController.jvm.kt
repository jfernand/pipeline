/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cr.pipeline.data.AppPreferences
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.nav.DeepLinkBus
import org.http4k.server.Http4kServer
import org.http4k.server.asServer

actual fun createMcpServerController(
    repository: JobApplicationRepository,
    preferencesStore: PreferencesStore,
    deepLinkBus: DeepLinkBus,
    logger: Logger,
): McpServerController = JvmMcpServerController(repository, preferencesStore, deepLinkBus, logger)

private const val LOOPBACK_HOST = "127.0.0.1"

private class JvmMcpServerController(
    private val repository: JobApplicationRepository,
    private val preferencesStore: PreferencesStore,
    private val deepLinkBus: DeepLinkBus,
    private val logger: Logger = Logger.withTag("McpServer"),
) : McpServerController {
    override val isSupported: Boolean = true

    private val _status = MutableStateFlow<McpServerStatus>(McpServerStatus.Stopped)
    private var server: Http4kServer? = null

    override fun observeStatus(): Flow<McpServerStatus> = _status.asStateFlow()

    override suspend fun start(port: Int) {
        val running = server
        if (running != null) {
            if (running.port() == port) {
                logger.d { "MCP server already running on port $port" }
                return
            }
            logger.d { "Restarting MCP server: ${running.port()} -> $port" }
            running.stop()
            server = null
        }
        try {
            logger.d { "Starting MCP server on $LOOPBACK_HOST..." }
            val started = buildMcpApp(repository, preferencesStore, deepLinkBus, logger).asServer(LoopbackNetty(port)).start()
            server = started
            val port = started.port()
            logger.d { "MCP server started on $LOOPBACK_HOST:$port" }
            _status.value = McpServerStatus.Running(LOOPBACK_HOST, port)
        } catch (e: Exception) {
            logger.e(e) { "Failed to start MCP server: ${e.message}" }
            _status.value = McpServerStatus.Error(e.message ?: "Failed to start MCP server")
        }
    }

    override suspend fun stop() {
        logger.d { "Stopping MCP server..." }
        server?.stop()
        server = null
        _status.value = McpServerStatus.Stopped
        logger.d { "MCP server stopped" }
    }
}
