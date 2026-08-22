#import "../template.typ": feature

#feature(
  designator: "PL-033",
  name: "Event Provenance",
  status: "Planned",
  summary: [
    Every event in the event log should record where it came from — the device id of the app
    instance that made the change, or an identifying token for the AI/client that made it through
    the MCP server — not just what changed.
  ],
  purpose: [
    `ApplicationEvent` and its variants (`ApplicationEvent.kt`) carry no origin field. With sync
    (PL-003, PL-025) and the MCP server (PL-013) both able to write the same event log a local edit
    does, there's no way to tell, after the fact, which device or which AI session made a given
    change — the event log and the Dev Tools view onto it (PL-012) show content, never who wrote
    it.
  ],
  related: (("PL-011", [Event-Sourced Local Storage]), ("PL-013", [MCP Server Infrastructure]), ("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)])),
)
