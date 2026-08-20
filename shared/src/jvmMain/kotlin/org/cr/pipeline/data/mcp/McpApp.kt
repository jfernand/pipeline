/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDate
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.nav.DeepLinkBus
import org.http4k.ai.mcp.ToolRequest
import org.http4k.ai.mcp.ToolResponse
import org.http4k.ai.mcp.model.Tool
import org.http4k.ai.mcp.model.ToolArgLensSpec
import org.http4k.ai.mcp.model.enum
import org.http4k.ai.mcp.model.localDate
import org.http4k.ai.mcp.model.string
import org.http4k.ai.mcp.protocol.ServerMetaData
import org.http4k.ai.mcp.server.security.NoMcpSecurity
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.lens.ParamMeta.IntegerParam
import org.http4k.routing.bind
import org.http4k.routing.mcpHttpNonStreaming

/**
 * The MCP capability surface — a plain [HttpHandler], with no networking of its own. Real serving
 * (binding a socket) is [JvmMcpServerController]'s job; this function exists separately so tests
 * can drive the exact same tool graph in-process, with no port ever opened.
 */
fun buildMcpApp(
    repository: JobApplicationRepository,
    preferencesStore: PreferencesStore,
    deepLinkBus: DeepLinkBus,
    logger: Logger = Logger.withTag("McpApp"),
): HttpHandler {
    val addApplication = Tool("add_application", "Add a new job application to Pipeline.", *applicationArgs(includeId = false))
    val editApplication = Tool(
        "edit_application",
        "Edit an existing job application in Pipeline, identified by id.",
        *applicationArgs(includeId = true),
    )
    val listApplications = Tool("list_applications", "List all job applications currently tracked in Pipeline.")
    val listSettings = Tool("list_settings", "List Pipeline's current app-level settings for this device.")
    val openApplication = Tool("open_application", "Open a job application's detail screen in the running Pipeline app.", idArg)

    val serverHandler = mcpHttpNonStreaming(
        ServerMetaData("pipeline", "1.0.0"),
        NoMcpSecurity,
        listApplications bind { _: ToolRequest ->
            logger.d { "MCP tool call: list_applications" }
            val applications = runBlocking { repository.observeApplications().first() }
            val message = if (applications.isEmpty()) {
                "No applications yet."
            } else {
                applications.joinToString("\n") { app ->
                    "#${app.id} ${app.company} — ${app.role} (${app.status}) — ${app.meta}"
                }
            }
            logger.d { "MCP tool response: list_applications -> ${applications.size} application(s)" }
            ToolResponse.Ok(message)
        },
        listSettings bind { _: ToolRequest ->
            logger.d { "MCP tool call: list_settings" }
            val prefs = runBlocking { preferencesStore.observePreferences().first() }
            val message = listOf(
                "Sync network mode: ${prefs.syncNetworkMode}",
                "Developer mode: ${if (prefs.developerMode) "on" else "off"}",
                "MCP server: ${if (prefs.mcpServerEnabled) "enabled" else "disabled"}, ${prefs.mcpServerAddress}:${prefs.mcpServerPort}",
            ).joinToString("\n")
            logger.d { "MCP tool response: list_settings -> $message" }
            ToolResponse.Ok(message)
        },
        addApplication bind { request: ToolRequest ->
            logger.d { "MCP tool call: add_application with args ${request.args}" }
            val input = request.toApplicationInput()
            val id = runBlocking { repository.saveApplication(null, input) }
            val message = "Added application #$id: ${input.company} — ${input.role} (${input.status})."
            logger.d { "MCP tool response: add_application -> #$id" }
            ToolResponse.Ok(message)
        },
        editApplication bind { request: ToolRequest ->
            val id = idArg(request)
            logger.d { "MCP tool call: edit_application with id=$id, args ${request.args}" }
            val input = request.toApplicationInput()
            runBlocking { repository.saveApplication(id, input) }
            val message = "Updated application #$id: ${input.company} — ${input.role} (${input.status})."
            logger.d { "MCP tool response: edit_application -> #$id" }
            ToolResponse.Ok(message)
        },
        openApplication bind { request: ToolRequest ->
            val id = idArg(request)
            logger.d { "MCP tool call: open_application with id=$id" }
            runBlocking { deepLinkBus.navigate("pipeline://app/$id") }
            val message = "Opened application #$id in Pipeline."
            logger.d { "MCP tool response: open_application -> #$id" }
            ToolResponse.Ok(message)
        },
    )

    val loggingFilter = Filter { next ->
        { request ->
            logger.d { "MCP HTTP request: ${request.method} ${request.uri}\n${request.bodyString()}" }
            val response = try {
                next(request)
            } catch (e: Throwable) {
                logger.e(e) { "MCP HTTP request failed: ${request.method} ${request.uri}" }
                throw e
            }
            logger.d { "MCP HTTP response: ${response.status}\n${response.bodyString()}" }
            response
        }
    }

    return loggingFilter.then(serverHandler)
}

private val companyArg = Tool.Arg.string().required("company", "Company name")
private val roleArg = Tool.Arg.string().required("role", "Job title / role")
private val statusArg = Tool.Arg.enum<AppStatus>().required("status", "One of: ${AppStatus.entries.joinToString { it.name }}")
private val dateAppliedArg = Tool.Arg.localDate().optional("dateApplied", "ISO-8601 date, e.g. 2026-08-14")
private val nextActionDateArg = Tool.Arg.localDate().optional("nextActionDate", "ISO-8601 date, e.g. 2026-08-14")
private val postingUrlArg = Tool.Arg.string().optional("postingUrl", "Job posting URL")
private val sourceArg = Tool.Arg.string().optional("source", "Where this application came from")
private val notesArg = Tool.Arg.string().defaulted("notes", "", "Freeform notes")

/**
 * A plain `Tool.Arg.long()` casts the decoded value straight to `Long` — but this app's MCP JSON
 * layer (Moshi-backed) normalizes any integer that fits in 32 bits down to a boxed `Int`
 * (verified by reading `MoshiNode`'s conversion logic directly), and ordinary application ids are
 * always in that range. `it as Long` would throw for every real id; going through `Number` first
 * handles whichever concrete numeric type actually comes back.
 */
private val idArg = ToolArgLensSpec.mapWithNewMeta({ (it as Number).toLong() }, { it }, IntegerParam)
    .required("id", "The application's id, as returned by add_application.")

private fun applicationArgs(includeId: Boolean) = buildList {
    if (includeId) add(idArg)
    addAll(listOf(companyArg, roleArg, statusArg, dateAppliedArg, nextActionDateArg, postingUrlArg, sourceArg, notesArg))
}.toTypedArray()

private fun ToolRequest.toApplicationInput() = ApplicationInput(
    company = companyArg(this),
    role = roleArg(this),
    status = statusArg(this),
    dateApplied = dateAppliedArg(this)?.toKotlinLocalDate(),
    nextActionDate = nextActionDateArg(this)?.toKotlinLocalDate(),
    postingUrl = postingUrlArg(this)?.takeIf { it.isNotBlank() },
    source = sourceArg(this)?.takeIf { it.isNotBlank() },
    notes = notesArg(this),
)
