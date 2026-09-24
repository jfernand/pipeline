#import "../template.typ": feature

#feature(
  designator: "PL-034",
  name: "Event Provenance Indicator",
  status: "Shipped",
  release: "MVP",
  summary: [
    A small icon — human (device) or AI (MCP client) — next to each entry in an application's
    status history, showing who or what made *that* change. A separate paperclip icon on the list
    card marks whether the application has any attachments at all.
  ],
  purpose: [
    PL-033 gives every event an origin, but recorded provenance nobody can see doesn't change how
    a user reads their history — a status change made by an agent overnight should look different
    from one made by hand. That's a fact about the event, though, not about the application as a
    whole: an application's history routinely spans both a device and an MCP client, so no single
    icon on a list card or a detail header can honestly stand for "who made this application."
    The original shipped version tried anyway — this corrects it to the granularity provenance
    actually has.
  ],
  description: [
    `ApplicationState.lastProvenance`, and the single `provenance` field it fed on `JobApplication`
    and `ApplicationDetail`, are gone. In their place, `StatusHistoryRecord` (and its read-model
    counterpart, `StatusHistoryEntry`) carry their own `provenance`, set in `applyEvent` from
    whichever event actually created that history entry — `ApplicationCreated`, `ApplicationEdited`
    (when it changes status), or `StatusChanged`. `StatusHistorySection`'s `Timeline` renders
    `ProvenanceIcon` next to each entry's date, so a history mixing device and MCP-client edits
    shows exactly that, entry by entry, instead of collapsing to whichever event happened to be
    last.

    `AppCard`'s slot that used to carry a provenance icon now shows a paperclip
    (`Icons.Filled.AttachFile`) when the application has any attachments — `JobApplication` gained
    `hasAttachments`, a real application-level fact, replacing the field that couldn't honestly be
    one. Both phone and tablet detail headers lost their single provenance icon+identifier line
    entirely; the per-event icons in the status history below are where that information actually
    belongs now, and `provenanceIdentifier` (the full `deviceId`/`clientId` text) was dead code
    once its only two callers were gone, so it's gone too.

    Demo/seed data (`DemoSeedingEventLog`) still always attributes `Device`, unchanged — the
    indicator only ever shows the `McpClient` glyph on history an MCP tool actually created.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationState.kt — StatusHistoryRecord.provenance",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationEventReducer.kt — sets it per history-appending branch, from that event's own provenance",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationStateMapping.kt — carries it onto StatusHistoryEntry; hasAttachments onto JobApplication",
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/JobApplication.kt — hasAttachments; ApplicationDetail.kt — StatusHistoryEntry.provenance",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/ProvenanceIndicator.kt — ProvenanceIcon, now per-event only",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/Timeline.kt, DetailSections.kt — ProvenanceIcon next to each status-history entry",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/AppCard.kt — the paperclip, gated on hasAttachments",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/DetailPane.kt, ui/phone/DetailScreen.kt — the removed header provenance line",
  ),
  related: (
    ("PL-033", [Event Provenance]),
    ("PL-002", [Browse Applications (List & Detail)]),
    ("PL-018", [Document Attachments (Resume & Cover Letter)]),
  ),
)
