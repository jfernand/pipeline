#import "../template.typ": feature

#feature(
  designator: "PL-003",
  name: "Device Pairing & Multi-Device Sync (UI Scaffold)",
  status: "Planned",
  release: "MVP",
  summary: [
    A pairing screen — QR-code placeholder, "scan to pair a tablet" — and a Sync destination on
    tablet. Neither does anything. No QR generation, no scanning, no event ever crosses a network.
  ],
  purpose: [
    A job search happens on more than one device, and "one tool" means one pipeline, not a copy per
    device left to drift. That is unmet until this ships. It is listed here, planned, precisely so
    PL-011's event log sitting underneath it does not get mistaken for the real thing.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/PairingScreen.kt — pairing UI, uses QrCodePlaceholder",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/QrCodePlaceholder.kt",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/TabletScreens.kt — TabletSyncContent",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/nav/Routes.kt — PairRoute, SyncRoute",
  ),
  related: (("PL-011", [Event-Sourced Local Storage]),),
)
