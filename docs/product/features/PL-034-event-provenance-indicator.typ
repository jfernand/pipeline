#import "../template.typ": feature

#feature(
  designator: "PL-034",
  name: "Event Provenance Indicator",
  status: "Shipped",
  release: "MVP",
  summary: [
    A small icon on each application card in the list — human (device) or AI (MCP client) —
    showing at a glance who or what made the application's most recent change, with the full
    identifier available in the detail view.
  ],
  purpose: [
    PL-033 gives every event an origin, but recorded provenance nobody can see doesn't change how
    a user reads their list — an application edited by an agent overnight should look different
    from one they edited themselves. This is the surface for that data: `AppCard` (PL-002) already
    carries a small icon slot for the job-source glyph; provenance gets the same treatment.
  ],
  description: [
    `ApplicationState` gained `lastProvenance` — set once, in `applyEvent`, from whichever event
    was just folded, rather than in each of its eight branches individually: every event type can
    be the most recent one, so setting it in one place after the per-event-type `when` keeps a
    future branch from being the one that forgets to. `toJobApplication`/`toApplicationDetail`
    carry it onto `JobApplication`/`ApplicationDetail` as `provenance`, unchanged.

    `ProvenanceIcon` (a small shared composable, `Person` for `Device`, `SmartToy` for
    `McpClient`, nothing for `Unknown`) and `provenanceIdentifier` (the full `deviceId`/`clientId`
    text) live in one file so `AppCard`'s list-card icon and both detail headers' icon-plus-text
    can't drift the way three separate copies could. `AppCard`'s own `sourceIcon` — a similar
    per-source icon lookup for the job-source glyph the summary references — turned out to be
    unused dead code once actually checked; left alone, out of scope here.

    Demo/seed data (`DemoSeedingEventLog`) always attributes `Device`, since it's seeded through
    the same repository path a real device edit would use — the indicator only ever shows the
    `McpClient` glyph on data an MCP tool actually touched.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationState.kt, ApplicationEventReducer.kt — lastProvenance",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationStateMapping.kt — toJobApplication/toApplicationDetail pass it through",
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/JobApplication.kt, ApplicationDetail.kt — the provenance field",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/ProvenanceIndicator.kt — ProvenanceIcon, provenanceIdentifier",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/AppCard.kt, ui/tablet/DetailPane.kt, ui/phone/DetailScreen.kt — where each is shown",
  ),
  related: (("PL-033", [Event Provenance]), ("PL-002", [Browse Applications (List & Detail)])),
)
