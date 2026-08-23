#import "../template.typ": feature

#feature(
  designator: "PL-035",
  name: "Opening Animation",
  status: "Planned",
  summary: [
    A shared-element/shared-outline transition for opening an application — list to detail —
    instead of a hard cut, whether the trigger is a tap in the list or a deep link (PL-009) landing
    directly on the detail screen.
  ],
  purpose: [
    Nothing in the app currently animates between screens; List → Detail is the one transition
    every session goes through repeatedly, so it's the one place a hard cut is most noticeable.
    Deep-linked opens (PL-017's MCP use, an OS notification) land cold, without a list row to
    animate from — the transition needs a sensible fallback for that path too, not just the
    in-list tap.
  ],
  related: (("PL-009", [In-App Navigation & Deep Links]), ("PL-002", [Browse Applications (List & Detail)]), ("PL-017", [Open Application (MCP Deep-Link Navigation)])),
)
