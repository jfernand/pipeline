#import "../template.typ": feature

#feature(
  designator: "PL-015",
  name: "List Applications (MCP)",
  status: "Shipped",
  release: "MVP",
  summary: [
    A read-only MCP tool. Every tracked application, as plain text: id, company, role, status, a
    short "Applied \<date\>" line. Same `observeApplications()` flow the UI's own list screens
    render from — not a second copy of the query.
  ],
  purpose: [
    An MCP client could add an application (PL-006) and edit one (PL-007) with no way to read one
    back. "What's in my pipeline" had one answer: ask a human to look at the screen. This tool is
    that gap, closed.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt — observeApplications()",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — list_applications MCP tool",
    "shared/src/jvmTest/kotlin/org/cr/pipeline/data/mcp/McpAppTest.kt — list_applications tests",
  ),
  related: (
    ("PL-006", [Add Application]),
    ("PL-007", [Edit Application]),
    ("PL-013", [MCP Server Infrastructure]),
    ("PL-016", [List Settings (MCP)]),
  ),
)
