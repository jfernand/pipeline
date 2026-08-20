#import "../template.typ": feature

#feature(
  designator: "PL-023",
  name: "Negative Search Filters",
  status: "Planned",
  summary: [
    A third state on every status filter chip: not set, set, set in the negative — each with its
    own look. Tapping cycles not-set → set → negative → not-set. "Negative" on Rejected reads as
    "show me everything except Rejected."
  ],
  purpose: [
    "Everything except Rejected" is a real question a positive-only filter cannot answer, short of
    tapping every other chip by hand. One chip, three states, says what a whole row of chips
    currently can't.
  ],
  implementation: (),
  related: (("PL-008", [Search & Filter Applications]),),
)
