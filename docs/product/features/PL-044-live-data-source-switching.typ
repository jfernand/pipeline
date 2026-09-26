#import "../template.typ": feature

#feature(
  designator: "PL-044",
  name: "Live Data-Source Switching",
  status: "Planned",
  summary: [
    Flipping "Show fake data" (PL-019) in Dev Tools switches the running app between the demo
    chain and the real one immediately — no restart — and does it through a route/MCP call, not
    only a tap on the switch.
  ],
  purpose: [
    PL-019's own screen already says it: "Restart the app for this to take effect." Today that's
    not a caveat, it's a wall — the switch writes a preference, but which chain backs the app is
    decided once, at startup (`PlatformDataModule`), so nothing changes on screen until a full
    process restart re-reads it. Capturing the fake-data list, then a real one, then fake again —
    for a screenshot, a demo, a test — means three cold restarts for three states that should be
    one tap apart. And because there's no route or MCP tool for the switch either (PL-041), an
    agent driving the app has no way to ask for it at all; a person has to reach for the physical
    device and the Dev Tools screen every time.
  ],
  implementation: (),
  related: (
    ("PL-019", [Fake Data Mode]),
    ("PL-012", [App Preferences & Developer Mode]),
    ("PL-041", [Routes and Deep Links]),
  ),
)
