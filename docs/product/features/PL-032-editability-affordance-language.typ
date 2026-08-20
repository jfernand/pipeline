#import "../template.typ": feature

#feature(
  designator: "PL-032",
  name: "Editability Affordance Language",
  status: "Planned",
  summary: [
    A distinct visual language for signaling that a small item — a note, a date, a field on the
    detail screen — can be edited in place. Not a pencil icon on everything; something that reads
    as part of the design system (PL-001) rather than a bolted-on affordance.
  ],
  purpose: [
    PL-002's notes and PL-007's application date are both moving toward inline editability. Without
    a shared signifier, each screen invents its own way to say "tap this to change it" — and the
    user can't tell an editable field from a static one until they try.
  ],
  implementation: (),
  related: (("PL-001", [Design System Foundation]), ("PL-002", [Browse Applications (List & Detail)]), ("PL-007", [Edit Application])),
)
