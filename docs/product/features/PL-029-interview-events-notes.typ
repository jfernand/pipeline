#import "../template.typ": feature

#feature(
  designator: "PL-029",
  name: "Interview Events & Notes",
  status: "Planned",
  summary: [
    A new event type on an application: an interview, at a time, with notes attached — distinct
    from a status change or a follow-up reminder. Integrates with PL-028's calendar sync.
  ],
  purpose: [
    A status of "Interviewing" names a state. It does not say when, with whom, or what was asked
    last time. This is the record status alone can't hold.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/SeedData.kt — SeedReminder, the closest existing shape (offset + message) this would extend",
  ),
  related: (("PL-005", [Update Application Status]), ("PL-028", [Calendar Integration])),
)
