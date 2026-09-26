/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import co.touchlab.kermit.Logger
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDate
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.nav.DeepLinkBus
import org.cr.pipeline.sync.event.EventProvenance
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
    val openLink = Tool(
        "open_link",
        "Open any screen or sheet in the running Pipeline app by its pipeline:// link — e.g. " +
            "pipeline://list?status=interviewing, pipeline://followups, pipeline://app/new, " +
            "pipeline://app/{id}/edit, pipeline://app/{id}?sheet=status (or contact, delete — delete " +
            "only asks, never deletes), pipeline://settings, pipeline://settings/pair, pipeline://sync, " +
            "pipeline://devtools.",
        linkArg,
    )
    val attachResume = Tool(
        "attach_resume",
        "Attach a résumé file to a job application, identified by id. Replaces the current résumé, if any.",
        idArg,
        filePathArg,
    )
    val attachCoverLetter = Tool(
        "attach_cover_letter",
        "Attach a cover letter file to a job application, identified by id. Replaces the current cover letter, if any.",
        idArg,
        filePathArg,
    )
    val attachFile = Tool(
        "attach_file",
        "Attach a miscellaneous file to a job application, identified by id. Doesn't replace any existing attachment.",
        idArg,
        filePathArg,
    )

    // The MCP transport (NoMcpSecurity, no session layer) doesn't hand this app a per-client or
    // per-session identifier to attribute a call to — "mcp" is the most specific token available
    // until that exists, but it's still enough to tell an AI-made change apart from one made
    // through the app itself.
    val mcpProvenance = EventProvenance.McpClient("mcp")

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
                    val (label, ago) = app.activity
                    "#${app.id} ${app.company} — ${app.role} (${app.status}) — $label $ago"
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
            val id = runBlocking { repository.saveApplication(null, input, mcpProvenance) }
            val message = "Added application #$id: ${input.company} — ${input.role} (${input.status})."
            logger.d { "MCP tool response: add_application -> #$id" }
            ToolResponse.Ok(message)
        },
        editApplication bind { request: ToolRequest ->
            val id = idArg(request)
            logger.d { "MCP tool call: edit_application with id=$id, args ${request.args}" }
            val input = request.toApplicationInput()
            runBlocking { repository.saveApplication(id, input, mcpProvenance) }
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
        // PL-041's MCP half: one tool reaching every route, through the same DeepLinkBus and
        // the same nav-graph deep links an OS link takes — not a tool per route, which would
        // duplicate the route table here and drift from it. A link the graph doesn't know is
        // ignored by the graph without a word, so the destination is checked here first, where
        // an error can still reach the caller.
        openLink bind { request: ToolRequest ->
            val link = linkArg(request).trim()
            logger.d { "MCP tool call: open_link with link=$link" }
            val destination = link.removePrefix(PIPELINE_SCHEME).substringBefore('?').substringBefore('/')
            if (!link.startsWith(PIPELINE_SCHEME) || destination !in LINK_DESTINATIONS) {
                logger.d { "MCP tool response: open_link -> rejected $link" }
                ToolResponse.Error(
                    "Not a Pipeline link: $link. Links start with $PIPELINE_SCHEME followed by one of " +
                        LINK_DESTINATIONS.joinToString() + ".",
                )
            } else {
                runBlocking { deepLinkBus.navigate(link) }
                logger.d { "MCP tool response: open_link -> $link" }
                ToolResponse.Ok("Opened $link in Pipeline.")
            }
        },
        attachResume bind { request: ToolRequest ->
            val id = idArg(request)
            val path = filePathArg(request)
            logger.d { "MCP tool call: attach_resume with id=$id, path=$path" }
            val file = readAttachableFile(path)
            if (file == null) {
                logger.d { "MCP tool response: attach_resume -> no file at $path" }
                ToolResponse.Error("No file found at $path")
            } else {
                runBlocking { repository.attachResume(id, file.name, file.readBytes(), mcpProvenance) }
                val message = "Attached résumé \"${file.name}\" to application #$id."
                logger.d { "MCP tool response: attach_resume -> #$id" }
                ToolResponse.Ok(message)
            }
        },
        attachCoverLetter bind { request: ToolRequest ->
            val id = idArg(request)
            val path = filePathArg(request)
            logger.d { "MCP tool call: attach_cover_letter with id=$id, path=$path" }
            val file = readAttachableFile(path)
            if (file == null) {
                logger.d { "MCP tool response: attach_cover_letter -> no file at $path" }
                ToolResponse.Error("No file found at $path")
            } else {
                runBlocking { repository.attachCoverLetter(id, file.name, file.readBytes(), mcpProvenance) }
                val message = "Attached cover letter \"${file.name}\" to application #$id."
                logger.d { "MCP tool response: attach_cover_letter -> #$id" }
                ToolResponse.Ok(message)
            }
        },
        attachFile bind { request: ToolRequest ->
            val id = idArg(request)
            val path = filePathArg(request)
            logger.d { "MCP tool call: attach_file with id=$id, path=$path" }
            val file = readAttachableFile(path)
            if (file == null) {
                logger.d { "MCP tool response: attach_file -> no file at $path" }
                ToolResponse.Error("No file found at $path")
            } else {
                runBlocking { repository.attachFile(id, file.name, file.readBytes(), mcpProvenance) }
                val message = "Attached \"${file.name}\" to application #$id."
                logger.d { "MCP tool response: attach_file -> #$id" }
                ToolResponse.Ok(message)
            }
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

private const val PIPELINE_SCHEME = "pipeline://"

/** The first path segment of every link the nav graphs answer (PipelineApp, PipelinePhoneApp). */
private val LINK_DESTINATIONS = listOf("app", "list", "followups", "settings", "sync", "devtools")

private val linkArg = Tool.Arg.string().required("link", "A pipeline:// link, e.g. pipeline://followups")

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

// PL-018: "not a raw file upload, but whatever shape the protocol treats as first-class" — the
// MCP server and the client it's serving always run on the same machine, so a filesystem path is
// that shape here, the same way open_application treats a deep link as first-class instead of
// re-implementing navigation over MCP.
private val filePathArg = Tool.Arg.string().required("path", "Absolute path, on this device, to the file to attach.")

private fun readAttachableFile(path: String): File? = File(path).takeIf { it.isFile }
