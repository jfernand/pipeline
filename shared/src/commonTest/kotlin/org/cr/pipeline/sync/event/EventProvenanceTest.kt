/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

package org.cr.pipeline.sync.event

import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.cr.pipeline.model.ApplicationInput
import org.cr.pipeline.model.AppStatus
import org.cr.pipeline.sync.chain.DeviceId
import kotlin.test.Test
import kotlin.test.assertEquals

class EventProvenanceTest {
    private val input = ApplicationInput(
        company = "Cedar & Byrne",
        role = "Senior Mobile Engineer",
        status = AppStatus.APPLIED,
        dateApplied = LocalDate(2026, 8, 1),
        nextActionDate = null,
        postingUrl = null,
        source = "LinkedIn",
        notes = "",
    )

    @Test
    fun `an event payload with no provenance key at all decodes as Unknown`() {
        // Simulates a real pre-PL-033 event log entry: encode a current event through the same
        // ApplicationEvent-typed path production code uses, then strip "provenance" back out,
        // since that's the one thing an old payload never had.
        val current: ApplicationEvent = ApplicationCreated(ApplicationId("app-1"), input, EventProvenance.Device(DeviceId("device-a")))
        val legacyPayload = JsonObject(Json.encodeToJsonElement(current).jsonObject.filterKeys { it != "provenance" }).toString()

        val decoded = Json.decodeFromString<ApplicationEvent>(legacyPayload)

        assertEquals(EventProvenance.Unknown, decoded.provenance)
    }

    @Test
    fun `Device and McpClient provenance round-trip through JSON unchanged`() {
        val device: ApplicationEvent = ApplicationCreated(ApplicationId("app-1"), input, EventProvenance.Device(DeviceId("device-a")))
        val mcp: ApplicationEvent = ApplicationCreated(ApplicationId("app-2"), input, EventProvenance.McpClient("mcp"))

        assertEquals(device, Json.decodeFromString<ApplicationEvent>(Json.encodeToString(device)))
        assertEquals(mcp, Json.decodeFromString<ApplicationEvent>(Json.encodeToString(mcp)))
    }
}
