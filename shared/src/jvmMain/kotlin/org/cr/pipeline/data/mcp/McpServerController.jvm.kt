package org.cr.pipeline.data.mcp

import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus

actual fun createMcpServerController(repository: JobApplicationRepository): McpServerController =
    JvmMcpServerController(repository)

private class JvmMcpServerController(
    private val repository: JobApplicationRepository,
) : McpServerController {
    override val isSupported: Boolean = true

    private val _status = MutableStateFlow<McpServerStatus>(McpServerStatus.Stopped)
    private var embeddedServer: EmbeddedServer<*, *>? = null

    override fun observeStatus(): Flow<McpServerStatus> = _status.asStateFlow()

    override suspend fun start() {
        if (embeddedServer != null) return
        val mcpServer = buildMcpServer(repository)
        try {
            val server = embeddedServer(CIO, port = 0, host = LOOPBACK_HOST) {
                mcpStreamableHttp { mcpServer }
            }
            server.startSuspend(wait = false)
            val port = server.engine.resolvedConnectors().first().port
            embeddedServer = server
            _status.value = McpServerStatus.Running(LOOPBACK_HOST, port)
        } catch (e: Exception) {
            _status.value = McpServerStatus.Error(e.message ?: "Failed to start MCP server")
        }
    }

    override suspend fun stop() {
        embeddedServer?.stopSuspend(gracePeriodMillis = 200, timeoutMillis = 1000)
        embeddedServer = null
        _status.value = McpServerStatus.Stopped
    }
}

private const val LOOPBACK_HOST = "127.0.0.1"

private val argsJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class AddApplicationArgs(
    val company: String,
    val role: String,
    val status: String,
    val dateApplied: String? = null,
    val nextActionDate: String? = null,
    val postingUrl: String? = null,
    val source: String? = null,
    val notes: String = "",
)

@Serializable
private data class EditApplicationArgs(
    val id: Long,
    val company: String,
    val role: String,
    val status: String,
    val dateApplied: String? = null,
    val nextActionDate: String? = null,
    val postingUrl: String? = null,
    val source: String? = null,
    val notes: String = "",
)

private fun applicationInputOf(
    company: String,
    role: String,
    status: String,
    dateApplied: String?,
    nextActionDate: String?,
    postingUrl: String?,
    source: String?,
    notes: String,
): ApplicationInput = ApplicationInput(
    company = company,
    role = role,
    status = AppStatus.valueOf(status.uppercase()),
    dateApplied = dateApplied?.let(LocalDate::parse),
    nextActionDate = nextActionDate?.let(LocalDate::parse),
    postingUrl = postingUrl?.takeIf { it.isNotBlank() },
    source = source?.takeIf { it.isNotBlank() },
    notes = notes,
)

private fun applicationSchemaProperties(includeId: Boolean): JsonObject = buildJsonObject {
    if (includeId) {
        put(
            "id",
            buildJsonObject {
                put("type", "integer")
                put("description", "The application's id, as returned by add_application.")
            },
        )
    }
    put("company", buildJsonObject { put("type", "string") })
    put("role", buildJsonObject { put("type", "string") })
    put(
        "status",
        buildJsonObject {
            put("type", "string")
            putJsonArray("enum") { AppStatus.entries.forEach { add(it.name) } }
        },
    )
    put("dateApplied", buildJsonObject { put("type", "string"); put("description", "ISO-8601 date, e.g. 2026-08-14") })
    put("nextActionDate", buildJsonObject { put("type", "string"); put("description", "ISO-8601 date, e.g. 2026-08-14") })
    put("postingUrl", buildJsonObject { put("type", "string") })
    put("source", buildJsonObject { put("type", "string") })
    put("notes", buildJsonObject { put("type", "string") })
}

private fun errorResult(message: String): CallToolResult =
    CallToolResult(content = listOf(TextContent(message)), isError = true)

private fun buildMcpServer(repository: JobApplicationRepository): Server {
    val server = Server(
        serverInfo = Implementation(name = "pipeline", version = "1.0.0"),
        options = ServerOptions(capabilities = ServerCapabilities(tools = ServerCapabilities.Tools())),
    )

    server.addTool(
        name = "add_application",
        description = "Add a new job application to Pipeline.",
        inputSchema = ToolSchema(
            properties = applicationSchemaProperties(includeId = false),
            required = listOf("company", "role", "status"),
        ),
    ) { request ->
        val args = try {
            argsJson.decodeFromJsonElement<AddApplicationArgs>(request.arguments ?: JsonObject(emptyMap()))
        } catch (e: Exception) {
            return@addTool errorResult("Invalid arguments: ${e.message}")
        }
        try {
            val input = applicationInputOf(
                args.company, args.role, args.status,
                args.dateApplied, args.nextActionDate, args.postingUrl, args.source, args.notes,
            )
            val id = repository.saveApplication(null, input)
            CallToolResult(content = listOf(TextContent("Added application #$id: ${args.company} — ${args.role} (${args.status}).")))
        } catch (e: Exception) {
            errorResult("Failed to add application: ${e.message}")
        }
    }

    server.addTool(
        name = "edit_application",
        description = "Edit an existing job application in Pipeline, identified by id.",
        inputSchema = ToolSchema(
            properties = applicationSchemaProperties(includeId = true),
            required = listOf("id", "company", "role", "status"),
        ),
    ) { request ->
        val args = try {
            argsJson.decodeFromJsonElement<EditApplicationArgs>(request.arguments ?: JsonObject(emptyMap()))
        } catch (e: Exception) {
            return@addTool errorResult("Invalid arguments: ${e.message}")
        }
        try {
            val input = applicationInputOf(
                args.company, args.role, args.status,
                args.dateApplied, args.nextActionDate, args.postingUrl, args.source, args.notes,
            )
            val id = repository.saveApplication(args.id, input)
            CallToolResult(content = listOf(TextContent("Updated application #$id: ${args.company} — ${args.role} (${args.status}).")))
        } catch (e: Exception) {
            errorResult("Failed to edit application: ${e.message}")
        }
    }

    return server
}
