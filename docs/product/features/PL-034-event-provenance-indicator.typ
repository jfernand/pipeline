#import "../template.typ": feature

#feature(
  designator: "PL-034",
  name: "Event Provenance Indicator",
  status: "Planned",
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
  related: (("PL-033", [Event Provenance]), ("PL-002", [Browse Applications (List & Detail)])),
)
