#import "../template.typ": feature

#feature(
  designator: "PL-036",
  name: "Advanced Filter Dialog",
  status: "Planned",
  release: "Backlog",
  summary: [
    A button next to the status chip row that opens a fuller filter dialog — multiple fields at
    once, not just the single status chip PL-008 offers today.
  ],
  purpose: [
    Text search plus status chips answers "where do things stand" up to a point, but a chip row
    can only ever narrow on one dimension at a time. Source, date-applied range, has-a-posting-URL
    — real questions a growing list needs answered together, not one filter reapplied by hand
    after the last one.
  ],
  related: (("PL-008", [Search & Filter Applications]), ("PL-023", [Negative Search Filters])),
)
