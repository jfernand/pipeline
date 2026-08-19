#import "../template.typ": feature

#feature(
  designator: "PL-013",
  name: "MCP Server Infrastructure",
  status: "Shipped",
  summary: [
    An in-process MCP server: http4k-ai-mcp-sdk, Netty engine, JVM desktop only. Bound to loopback
    (`127.0.0.1`), started and stopped with the app via a Settings toggle — address and port
    configurable, request/response logged through `Logger`. Every other platform gets a stub
    (`UnsupportedMcpServerController`, `isSupported = false`): no loopback socket binding on
    Android/iOS/js/wasmJs, so there's nothing to implement there.
  ],
  purpose: [
    Every MCP tool — PL-006, PL-007, PL-015, PL-016, PL-017, whatever comes next — is a route on the
    `HttpHandler` this serves. No server, no tools, no agent access. This is the whole "expose
    Pipeline to an agent" half of the mission, running.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/mcp/McpServerController.kt — expect createMcpServerController(...), McpServerStatus",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpServerController.jvm.kt — real JVM implementation",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — the tool graph, as a plain HttpHandler",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/LoopbackNetty.kt — loopback-only Netty binding",
    "shared/src/commonMain/kotlin/org/cr/pipeline/di/McpDataModule.kt — Koin wiring",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/SettingsScreen.kt — server toggle/address row",
    "shared/src/jvmTest/kotlin/org/cr/pipeline/data/mcp/McpAppTest.kt, McpServerControllerTest.kt — in-process tests, no socket needed",
    "gradle/libs.versions.toml — http4k-bom 6.57.2.0; http4k-ai-mcp-sdk moved to org.http4k.pro at 6.17.0.0, http4k Commercial License (free tier), not Apache 2",
  ),
  related: (("PL-006", [Add Application]), ("PL-007", [Edit Application]), ("PL-012", [App Preferences & Developer Mode])),
)
