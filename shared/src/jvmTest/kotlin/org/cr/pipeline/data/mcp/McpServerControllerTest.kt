/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.data.mcp

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.cr.pipeline.data.EventSourcedJobApplicationRepository
import org.cr.pipeline.data.FakeApplicationStateStore
import org.cr.pipeline.sync.event.InMemoryEventLog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class McpServerControllerTest {
    private class RecordingLogWriter : LogWriter() {
        val records = mutableListOf<Record>()
        data class Record(val severity: Severity, val message: String, val tag: String)

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            records.add(Record(severity, message, tag))
        }
    }

    @Test
    fun `starts and stops server while logging debug messages`() = runTest {
        val store = FakeApplicationStateStore()
        val writer = RecordingLogWriter()
        val testLogger = Logger(
            config = StaticConfig(
                minSeverity = Severity.Debug,
                logWriterList = listOf(writer),
            ),
            tag = "McpServerTest",
        )
        val repository = EventSourcedJobApplicationRepository(store, InMemoryEventLog())
        val controller = createMcpServerController(repository, FakePreferencesStore(), FakeDeepLinkBus(), testLogger)

        assertTrue(controller.isSupported)

        controller.start(0)
        val runningStatus = controller.observeStatus().first { it is McpServerStatus.Running }
        assertIs<McpServerStatus.Running>(runningStatus)
        assertEquals("127.0.0.1", runningStatus.host)
        assertTrue(runningStatus.port > 0)

        assertTrue(writer.records.any { it.message.contains("Starting MCP server") && it.severity == Severity.Debug })
        assertTrue(writer.records.any { it.message.contains("MCP server started on 127.0.0.1") && it.severity == Severity.Debug })

        controller.stop()
        val stoppedStatus = controller.observeStatus().first { it is McpServerStatus.Stopped }
        assertIs<McpServerStatus.Stopped>(stoppedStatus)

        assertTrue(writer.records.any { it.message.contains("Stopping MCP server") && it.severity == Severity.Debug })
        assertTrue(writer.records.any { it.message.contains("MCP server stopped") && it.severity == Severity.Debug })
    }
}
