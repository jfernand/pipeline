#import "../template.typ": feature

#feature(
  designator: "PL-025",
  name: "Multi-Device Sync Service",
  status: "Planned",
  release: "MVP",
  summary: [
    The transport PL-003's pairing screen has been waiting on. An unsynced app can turn sync on,
    which presents a QR code carrying an iroh ticket — the sync key. A second device scans it, and
    both sides start exchanging the event logs PL-011 already keeps.
  ],
  purpose: [
    PL-011 built the event log that makes divergence-safe merging possible. PL-003 built the
    screen that promises it. This is what actually moves an event from one device's log to
    another's — without it, "one pipeline, not a copy per device" is a mission statement, not a
    feature.
  ],
  implementation: (),
  related: (("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)]), ("PL-011", [Event-Sourced Local Storage])),
)
