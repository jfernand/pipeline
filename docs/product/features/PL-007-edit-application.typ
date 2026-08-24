#import "../template.typ": feature

#feature(
  designator: "PL-007",
  name: "Edit Application",
  status: "Shipped",
  release: "MVP",
  summary: [
    Updates an application in place, by id. Same fields as PL-006, plus the id. Two entry points —
    the Add/Edit screen, opened from an existing application, and the `edit_application` MCP tool.
  ],
  purpose: [
    A phone screen gets scheduled. Notes pile up. A next-action date moves. Without this, the
    application record is write-once — accurate on day one, wrong by week two. The MCP tool lets an
    agent fix a record without the user re-entering the form.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt — saveApplication(id, input)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/EventSourcedJobApplicationRepository.kt — turns the call into an ApplicationEdited event",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/screens/AddEditScreen.kt — Compose UI form",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — edit_application MCP tool",
  ),
  related: (("PL-006", [Add Application]), ("PL-005", [Update Application Status]), ("PL-015", [List Applications (MCP)])),
)
