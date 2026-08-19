#import "../template.typ": feature

#feature(
  designator: "PL-017",
  name: "Open Application (MCP Deep-Link Navigation)",
  status: "Shipped",
  summary: [
    An action MCP tool. Pushes a `pipeline://app/{id}` deep link onto a shared `DeepLinkBus`. The
    running phone or tablet UI collects it and feeds it into
    `NavHostController.handleDeepLink(...)` — the same call a cold-start deep link already uses.
    An MCP client can drive the live app to an application's detail screen.
  ],
  purpose: [
    PL-015 and PL-016 let an agent read Pipeline's state. This is the first tool that reaches past
    the data and acts on the running window itself. "Show me the Rocket application" moves the
    screen the user is looking at — it does not just print an answer here and leave the app
    untouched.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/nav/DeepLinkBus.kt — the event bus (interface + DefaultDeepLinkBus)",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — open_application MCP tool",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/PipelinePhoneApp.kt — deep-link collector (phone)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/PipelineApp.kt — deep-link collector (tablet)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/nav/Routes.kt — DetailRoute, the navigation target",
  ),
  related: (("PL-009", [In-App Navigation & Deep Links]), ("PL-013", [MCP Server Infrastructure]), ("PL-015", [List Applications (MCP)])),
)
