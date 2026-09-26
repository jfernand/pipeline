/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import org.cr.pipeline.data.AppPreferences
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.FakeApplicationStateStore
import org.cr.pipeline.data.PreferencesStore
import org.cr.pipeline.data.SyncNetworkMode
import org.cr.pipeline.data.io.FakeFileArchiveService
import org.cr.pipeline.data.io.FileArchiveService
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.nav.DeepLinkBus
import org.cr.pipeline.sync.event.ApplicationEvent
import org.cr.pipeline.sync.event.EventProvenance
import org.cr.pipeline.sync.event.InMemoryEventLog
import org.http4k.ai.mcp.McpError
import org.http4k.ai.mcp.ToolRequest
import org.http4k.ai.mcp.ToolResponse
import org.http4k.ai.mcp.client.http.HttpNonStreamingMcpClient
import org.http4k.ai.mcp.model.Content
import org.http4k.ai.model.ToolName
import org.http4k.core.Uri
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** [dev.forkhandles.result4k]'s own `get()` only applies to `Result<T, T>` (value and error the
 *  same type) — on a `Result<T, E>` with T != E it still silently type-checks by widening both
 *  to `Any` via covariance, discarding the failure's real type. `orThrow` is the correct unwrap
 *  for mismatched types. */
private fun <T> Result4k<T, McpError>.orFail(): T = orThrow { AssertionError(it.toString()) }

private class RecordingLogWriter : LogWriter() {
    val records = mutableListOf<Record>()
    data class Record(val severity: Severity, val message: String, val tag: String)

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        records.add(Record(severity, message, tag))
    }
}

internal class FakePreferencesStore(initial: AppPreferences = AppPreferences()) : PreferencesStore {
    private val state = MutableStateFlow(initial)

    override fun observePreferences(): Flow<AppPreferences> = state

    override val currentPreferences: AppPreferences get() = state.value

    override suspend fun setSyncNetworkMode(mode: SyncNetworkMode) {
        state.value = state.value.copy(syncNetworkMode = mode)
    }

    override suspend fun setDeveloperMode(enabled: Boolean) {
        state.value = state.value.copy(developerMode = enabled)
    }

    override suspend fun setShowFakeData(enabled: Boolean) {
        state.value = state.value.copy(showFakeData = enabled)
    }

    override suspend fun setMcpServerEnabled(enabled: Boolean) {
        state.value = state.value.copy(mcpServerEnabled = enabled)
    }

    override suspend fun setMcpServerPort(port: Int) {
        state.value = state.value.copy(mcpServerPort = port)
    }

    override suspend fun setLastDetailApplicationId(id: Long?) {
        state.value = state.value.copy(lastDetailApplicationId = id)
    }
}

internal class FakeDeepLinkBus : DeepLinkBus {
    val navigatedTo = mutableListOf<String>()

    override val deepLinks: SharedFlow<String> = MutableSharedFlow()

    override suspend fun navigate(deepLink: String) {
        navigatedTo.add(deepLink)
    }
}

/**
 * Stands up the real MCP app and drives it entirely in-process: [buildMcpApp] returns a plain
 * `HttpHandler`, and http4k's own [HttpNonStreamingMcpClient] accepts any `HttpHandler` in place
 * of a real [org.http4k.client.JavaHttpClient] — so this never opens a socket, never binds a
 * port, and still exercises the exact request/response path a real MCP client would use.
 */
class McpAppTest {
    private fun clientAgainst(
        store: FakeApplicationStateStore,
        preferencesStore: PreferencesStore = FakePreferencesStore(),
        deepLinkBus: DeepLinkBus = FakeDeepLinkBus(),
        fileService: FileArchiveService = FakeFileArchiveService(),
    ): HttpNonStreamingMcpClient {
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog(), fileService)
        return HttpNonStreamingMcpClient(
            Uri.of("http://in-memory/mcp"),
            http = buildMcpApp(repository, preferencesStore, deepLinkBus),
        ).also { it.start().orFail() }
    }

    @Test
    fun `lists add_application, edit_application, list_applications, list_settings, open_application, open_link, and the three attach tools`() {
        val client = clientAgainst(FakeApplicationStateStore())

        val names = client.tools().list().orFail().map { it.name.value }

        assertEquals(
            setOf(
                "add_application", "edit_application", "list_applications", "list_settings", "open_application",
                "open_link", "attach_resume", "attach_cover_letter", "attach_file",
            ),
            names.toSet(),
        )
    }

    @Test
    fun `list_applications reports no applications yet when the store is empty`() {
        val client = clientAgainst(FakeApplicationStateStore())

        val result = client.tools().call(ToolName.of("list_applications"), ToolRequest(emptyMap())).orFail()

        assertIs<ToolResponse.Ok>(result)
        val message = result.content.orEmpty().single()
        assertIs<Content.Text>(message)
        assertEquals("No applications yet.", message.text)
    }

    @Test
    fun `list_applications reflects applications added through the real repository`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()

        val result = client.tools().call(ToolName.of("list_applications"), ToolRequest(emptyMap())).orFail()

        assertIs<ToolResponse.Ok>(result)
        val message = result.content.orEmpty().single()
        assertIs<Content.Text>(message)
        assertTrue(message.text.contains("#$id"))
        assertTrue(message.text.contains("Acme Rockets"))
        assertTrue(message.text.contains("Staff Engineer"))
        assertTrue(message.text.contains("APPLIED"))
    }

    @Test
    fun `list_settings reports the current preferences`() {
        val prefs = AppPreferences(
            syncNetworkMode = SyncNetworkMode.ANY_NETWORK,
            developerMode = true,
            mcpServerEnabled = true,
            mcpServerAddress = "localhost",
            mcpServerPort = 34687,
        )
        val client = clientAgainst(FakeApplicationStateStore(), FakePreferencesStore(prefs))

        val result = client.tools().call(ToolName.of("list_settings"), ToolRequest(emptyMap())).orFail()

        assertIs<ToolResponse.Ok>(result)
        val message = result.content.orEmpty().single()
        assertIs<Content.Text>(message)
        assertTrue(message.text.contains("ANY_NETWORK"))
        assertTrue(message.text.contains("Developer mode: on"))
        assertTrue(message.text.contains("enabled"))
        assertTrue(message.text.contains("localhost:34687"))
    }

    @Test
    fun `open_application pushes a deep link for the given id onto the bus`() {
        val store = FakeApplicationStateStore()
        val deepLinkBus = FakeDeepLinkBus()
        val client = clientAgainst(store, deepLinkBus = deepLinkBus)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()

        val result = client.tools().call(ToolName.of("open_application"), ToolRequest(mapOf("id" to id))).orFail()

        assertIs<ToolResponse.Ok>(result)
        assertEquals(listOf("pipeline://app/$id"), deepLinkBus.navigatedTo)
    }

    @Test
    fun `open_application without an id is rejected, not silently navigated`() {
        val deepLinkBus = FakeDeepLinkBus()
        val client = clientAgainst(FakeApplicationStateStore(), deepLinkBus = deepLinkBus)

        val result = client.tools().call(ToolName.of("open_application"), ToolRequest(emptyMap()))

        assertIs<Failure<*>>(result)
        assertEquals(emptyList(), deepLinkBus.navigatedTo)
    }

    @Test
    fun `open_link pushes a pipeline link onto the bus as given`() {
        val deepLinkBus = FakeDeepLinkBus()
        val client = clientAgainst(FakeApplicationStateStore(), deepLinkBus = deepLinkBus)
        val links = listOf("pipeline://followups", "pipeline://list?status=interviewing", "pipeline://app/7?sheet=status")

        links.forEach { link ->
            val result = client.tools().call(ToolName.of("open_link"), ToolRequest(mapOf("link" to link))).orFail()
            assertIs<ToolResponse.Ok>(result)
        }

        assertEquals(links, deepLinkBus.navigatedTo)
    }

    @Test
    fun `open_link rejects other schemes and unknown destinations without navigating`() {
        val deepLinkBus = FakeDeepLinkBus()
        val client = clientAgainst(FakeApplicationStateStore(), deepLinkBus = deepLinkBus)

        listOf("https://example.com", "pipeline://nowhere", "pipeline:/list", "list").forEach { link ->
            val result = client.tools().call(ToolName.of("open_link"), ToolRequest(mapOf("link" to link))).orFail()
            assertIs<ToolResponse.Error>(result)
        }

        assertEquals(emptyList(), deepLinkBus.navigatedTo)
    }

    @Test
    fun `attach_resume reads the file at path and attaches it, replacing any prior resume`() {
        val store = FakeApplicationStateStore()
        val fileService = FakeFileArchiveService()
        val client = clientAgainst(store, fileService = fileService)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()
        val resume = File.createTempFile("resume", ".pdf").apply { writeText("resume v1") }

        try {
            val result = client.tools().call(ToolName.of("attach_resume"), ToolRequest(mapOf("id" to id, "path" to resume.absolutePath))).orFail()

            assertIs<ToolResponse.Ok>(result)
            assertEquals(1, store.states.getValue(id).attachments.size)
            assertEquals(resume.name, store.states.getValue(id).attachments.single().fileName)
            assertEquals(1, fileService.writes.size)
        } finally {
            resume.delete()
        }
    }

    @Test
    fun `attach_cover_letter reads the file at path and attaches it, replacing any prior cover letter`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()
        val coverLetter = File.createTempFile("cover-letter", ".pdf").apply { writeText("cover letter") }

        try {
            val result = client.tools().call(ToolName.of("attach_cover_letter"), ToolRequest(mapOf("id" to id, "path" to coverLetter.absolutePath))).orFail()

            assertIs<ToolResponse.Ok>(result)
            assertEquals(coverLetter.name, store.states.getValue(id).attachments.single().fileName)
        } finally {
            coverLetter.delete()
        }
    }

    @Test
    fun `attach_file reads the file at path and appends it without replacing anything`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()
        val fileA = File.createTempFile("misc-a", ".txt").apply { writeText("a") }
        val fileB = File.createTempFile("misc-b", ".txt").apply { writeText("b") }

        try {
            client.tools().call(ToolName.of("attach_file"), ToolRequest(mapOf("id" to id, "path" to fileA.absolutePath))).orFail()
            client.tools().call(ToolName.of("attach_file"), ToolRequest(mapOf("id" to id, "path" to fileB.absolutePath))).orFail()

            assertEquals(
                listOf(fileA.name, fileB.name),
                store.states.getValue(id).attachments.map { it.fileName },
            )
        } finally {
            fileA.delete()
            fileB.delete()
        }
    }

    @Test
    fun `attach_resume with a path that doesn't exist returns an error, not silently ignored`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()

        val result = client.tools().call(ToolName.of("attach_resume"), ToolRequest(mapOf("id" to id, "path" to "/no/such/file.pdf"))).orFail()

        assertIs<ToolResponse.Error>(result)
        assertEquals(emptyList(), store.states.getValue(id).attachments)
    }

    @Test
    fun `add_application creates an application through the real event-sourced repository`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)

        val result = client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(
                mapOf(
                    "company" to "Acme Rockets",
                    "role" to "Staff Engineer",
                    "status" to "APPLIED",
                    "notes" to "via MCP test",
                ),
            ),
        ).orFail()

        assertIs<ToolResponse.Ok>(result)
        val (id, state) = store.states.entries.single()
        assertEquals("Acme Rockets", state.company)
        assertEquals(AppStatus.APPLIED, state.status)
        assertEquals("Application created", state.statusHistory.single().note)
        val message = result.content.orEmpty().single()
        assertIs<Content.Text>(message)
        assertTrue(message.text.contains("Acme Rockets"))
        assertTrue(message.text.contains("#$id"))
    }

    @Test
    fun `add_application and edit_application tag their events with MCP provenance, not Device`() {
        val store = FakeApplicationStateStore()
        val eventLog = InMemoryEventLog()
        val repository = EventSourcedJobApplicationRepository(store, eventLog)
        val client = HttpNonStreamingMcpClient(
            Uri.of("http://in-memory/mcp"),
            http = buildMcpApp(repository, FakePreferencesStore(), FakeDeepLinkBus()),
        ).also { it.start().orFail() }

        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()
        client.tools().call(
            ToolName.of("edit_application"),
            ToolRequest(mapOf("id" to id, "company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "INTERVIEW")),
        ).orFail()

        val chain = runBlocking { eventLog.observeChain().first() }
        val provenances = chain.map { Json.decodeFromString<ApplicationEvent>(it.payload).provenance }
        assertEquals(2, provenances.size)
        provenances.forEach { assertIs<EventProvenance.McpClient>(it) }
    }

    @Test
    fun `edit_application updates the existing application in place, not a duplicate`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()
        val id = store.states.keys.single()

        val result = client.tools().call(
            ToolName.of("edit_application"),
            ToolRequest(
                mapOf(
                    "id" to id,
                    "company" to "Acme Rockets",
                    "role" to "Staff Engineer",
                    "status" to "INTERVIEW",
                ),
            ),
        ).orFail()

        assertIs<ToolResponse.Ok>(result)
        assertEquals(1, store.states.size)
        assertEquals(AppStatus.INTERVIEW, store.states.getValue(id).status)
        assertEquals(2, store.states.getValue(id).statusHistory.size)
    }

    @Test
    fun `add_application without a required argument is rejected, not silently accepted`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)

        val result = client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("role" to "Staff Engineer", "status" to "APPLIED")),
        )

        assertIs<Failure<*>>(result)
        assertEquals(emptyMap(), store.states)
    }

    @Test
    fun `edit_application without an id is rejected, not silently applied`() {
        val store = FakeApplicationStateStore()
        val client = clientAgainst(store)
        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "APPLIED")),
        ).orFail()

        val result = client.tools().call(
            ToolName.of("edit_application"),
            ToolRequest(mapOf("company" to "Acme Rockets", "role" to "Staff Engineer", "status" to "OFFER")),
        )

        assertIs<Failure<*>>(result)
        assertEquals(AppStatus.APPLIED, store.states.values.single().status)
    }

    @Test
    fun `debug logs MCP requests and tool execution`() {
        val store = FakeApplicationStateStore()
        val writer = RecordingLogWriter()
        val testLogger = Logger(
            config = StaticConfig(
                minSeverity = Severity.Debug,
                logWriterList = listOf(writer),
            ),
            tag = "McpAppTest",
        )
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val client = HttpNonStreamingMcpClient(
            Uri.of("http://in-memory/mcp"),
            http = buildMcpApp(repository, FakePreferencesStore(), FakeDeepLinkBus(), testLogger),
        )
        client.start().orFail()

        client.tools().call(
            ToolName.of("add_application"),
            ToolRequest(
                mapOf(
                    "company" to "Acme Rockets",
                    "role" to "Staff Engineer",
                    "status" to "APPLIED",
                ),
            ),
        ).orFail()

        assertTrue(writer.records.any { it.message.contains("MCP tool call: add_application") && it.severity == Severity.Debug })
        assertTrue(writer.records.any { it.message.contains("MCP tool response: add_application") && it.severity == Severity.Debug })
        assertTrue(writer.records.any { it.message.contains("MCP HTTP request") && it.severity == Severity.Debug })
        assertTrue(writer.records.any { it.message.contains("MCP HTTP response") && it.severity == Severity.Debug })
    }
}
