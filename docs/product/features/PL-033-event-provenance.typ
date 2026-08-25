#import "../template.typ": feature

#feature(
  designator: "PL-033",
  name: "Event Provenance",
  status: "Shipped",
  release: "MVP",
  summary: [
    Every event in the event log records where it came from — the device that made an ordinary
    in-app edit, or an identifying token for the AI/client that made it through the MCP server —
    not just what changed. A third, `Unknown` case covers event payloads written before this field
    existed, so old event logs keep reading correctly.
  ],
  purpose: [
    `ApplicationEvent` and its variants (`ApplicationEvent.kt`) carried no origin field. With the
    MCP server (PL-013) able to write the same event log a local edit does, there was no way to
    tell, after the fact, which side made a given change — the event log and the Dev Tools view
    onto it (PL-012) show content, never who wrote it.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/EventProvenance.kt — the sealed Device(deviceId)/McpClient(clientId)/Unknown type",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationEvent.kt — a provenance field, defaulted to Unknown so old payloads without the key still decode",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt, EventSourcedJobApplicationRepository.kt — saveApplication/updateStatus take an optional provenance; left null, it resolves to this device's own DeviceId",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — add_application/edit_application tag their events McpClient instead of the default",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/DeviceIdentityStore.kt — expect fun createDeviceIdentityStore(), with an actual per platform (android/ios/jvm/js/wasmJs) backed by that platform's Settings, so the same device id generation/persistence applies everywhere instead of a Room table only some platforms had",
  ),
  related: (("PL-011", [Event-Sourced Local Storage]), ("PL-013", [MCP Server Infrastructure]), ("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)])),
)
