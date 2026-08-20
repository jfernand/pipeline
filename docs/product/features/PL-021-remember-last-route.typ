#import "../template.typ": feature

#feature(
  designator: "PL-021",
  name: "Remember Last Route",
  status: "Planned",
  summary: [
    On launch, the app returns to whatever screen it was on when it closed — not always the list.
    Stored in preferences, restored before the first frame the user sees.
  ],
  purpose: [
    Closing on an application's detail screen and reopening to the list, every single time, is a
    small tax paid on every launch. Preferences already persist device settings (PL-012); a route
    is one more value.
  ],
  implementation: (),
  related: (
    ("PL-002", [Browse Applications (List & Detail)]),
    ("PL-009", [In-App Navigation & Deep Links]),
    ("PL-012", [App Preferences & Developer Mode]),
  ),
)
