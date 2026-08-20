#import "../template.typ": feature

#feature(
  designator: "PL-019",
  name: "Fake Data Mode",
  status: "Planned",
  summary: [
    A Dev Tools switch that flips the app's repository and event log to in-memory
    implementations, seeded from the same `SeedData` used everywhere else. Flip it back, and the
    real Room-backed data returns untouched.
  ],
  purpose: [
    Testing a UI change against real data risks the real data. This gives Dev Tools a sandbox that
    looks exactly like production and can't touch it — the one thing missing from PL-012's Dev
    Tools screen.
  ],
  implementation: (),
  related: (
    ("PL-004", [Local Application Database]),
    ("PL-011", [Event-Sourced Local Storage]),
    ("PL-012", [App Preferences & Developer Mode]),
  ),
)
