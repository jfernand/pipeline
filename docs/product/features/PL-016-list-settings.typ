#import "../template.typ": feature

#feature(
  designator: "PL-016",
  name: "List Settings (MCP)",
  status: "Shipped",
  release: "MVP",
  summary: [
    A read-only MCP tool. Reports the device's current preferences — sync network mode, developer
    mode, and whether and where the MCP server itself is listening.
  ],
  purpose: [
    "Is the MCP server reachable from other devices" is a question about the app talking to
    itself. An agent should be able to ask it directly, not relay it through a user reading their
    own Settings screen out loud. Any future per-device setting gets exposed the same way.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/PreferencesStore.kt — observePreferences()",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/AppPreferences.kt — the settings being reported",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — list_settings MCP tool",
    "shared/src/jvmTest/kotlin/org/cr/pipeline/data/mcp/McpAppTest.kt — list_settings tests",
  ),
  related: (("PL-012", [App Preferences & Developer Mode]), ("PL-013", [MCP Server Infrastructure]), ("PL-015", [List Applications (MCP)])),
)
