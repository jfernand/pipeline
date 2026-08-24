#import "../template.typ": feature

#feature(
  designator: "PL-006",
  name: "Add Application",
  status: "Shipped",
  release: "MVP",
  summary: [
    Creates a job application: company, role, status, and optionally applied/next-action dates,
    posting URL, source, notes. Persisted through the event-sourced repository. Two entry points —
    the Add/Edit screen, and the `add_application` MCP tool.
  ],
  purpose: [
    Without this, there is nothing to track — it is the entry point, full stop. Because it is also
    an MCP tool, "add Rocket Companies" logs an application without the user ever opening the app.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt — saveApplication(id = null, input)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/EventSourcedJobApplicationRepository.kt — turns the call into an ApplicationCreated event",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/screens/AddEditScreen.kt — Compose UI form",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — add_application MCP tool",
  ),
  related: (
    ("PL-004", [Local Application Database]),
    ("PL-007", [Edit Application]),
    ("PL-015", [List Applications (MCP)]),
  ),
)
