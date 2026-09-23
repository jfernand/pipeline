#import "../template.typ": feature

#feature(
  designator: "PL-043",
  name: "Remember List Filter",
  status: "Planned",
  summary: [
    The search text and status filter chips on the applications list (PL-008) survive leaving the
    screen and coming back — opening a detail, backing out, switching tabs, relaunching the app.
  ],
  purpose: [
    "Where do things stand" (PL-002) is the point of the list, and search/filter (PL-008) is how
    that answer narrows past a handful of applications. Losing that narrowing every time a detail
    is opened and closed means re-typing the same search or re-tapping the same chips dozens of
    times a session — the filter should hold state the way PL-021 already keeps the last-viewed
    route.
  ],
  implementation: (),
  related: (
    ("PL-002", [Browse Applications (List & Detail)]),
    ("PL-008", [Search & Filter Applications]),
    ("PL-021", [Remember Last Route]),
  ),
)
