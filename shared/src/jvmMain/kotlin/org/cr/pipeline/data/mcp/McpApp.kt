package org.cr.pipeline.data.mcp

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDate
import org.cr.pipeline.data.JobApplicationRepository
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.http4k.ai.mcp.ToolRequest
import org.http4k.ai.mcp.ToolResponse
import org.http4k.ai.mcp.model.Tool
import org.http4k.ai.mcp.model.ToolArgLensSpec
import org.http4k.ai.mcp.model.enum
import org.http4k.ai.mcp.model.localDate
import org.http4k.ai.mcp.model.string
import org.http4k.ai.mcp.protocol.ServerMetaData
import org.http4k.ai.mcp.server.security.NoMcpSecurity
import org.http4k.core.HttpHandler
import org.http4k.lens.ParamMeta.IntegerParam
import org.http4k.routing.bind
import org.http4k.routing.mcpHttpNonStreaming

/**
 * The MCP capability surface — a plain [HttpHandler], with no networking of its own. Real serving
 * (binding a socket) is [JvmMcpServerController]'s job; this function exists separately so tests
 * can drive the exact same tool graph in-process, with no port ever opened.
 */
fun buildMcpApp(repository: JobApplicationRepository): HttpHandler {
    val addApplication = Tool("add_application", "Add a new job application to Pipeline.", *applicationArgs(includeId = false))
    val editApplication = Tool(
        "edit_application",
        "Edit an existing job application in Pipeline, identified by id.",
        *applicationArgs(includeId = true),
    )

    return mcpHttpNonStreaming(
        ServerMetaData("pipeline", "1.0.0"),
        NoMcpSecurity,
        addApplication bind { request: ToolRequest ->
            val input = request.toApplicationInput()
            val id = runBlocking { repository.saveApplication(null, input) }
            ToolResponse.Ok("Added application #$id: ${input.company} — ${input.role} (${input.status}).")
        },
        editApplication bind { request: ToolRequest ->
            val id = idArg(request)
            val input = request.toApplicationInput()
            runBlocking { repository.saveApplication(id, input) }
            ToolResponse.Ok("Updated application #$id: ${input.company} — ${input.role} (${input.status}).")
        },
    )
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
