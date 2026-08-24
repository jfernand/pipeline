#import "../template.typ": feature

#feature(
  designator: "PL-038",
  name: "Sync Key QR Generation",
  status: "Planned",
  release: "MVP",
  summary: [
    A real, scannable QR code encoding the iroh ticket PL-025's sync system generates — replacing
    `QrCodePlaceholder`'s deterministic pseudo-QR pattern, which looks like a code but decodes to
    nothing.
  ],
  purpose: [
    PL-003's pairing screen and PL-025's sync flow both show a QR code today, but it's a fixed,
    seeded grid pattern for the design mock — not an encoding of anything. Pairing a second device
    means it has to actually scan a real code carrying the real ticket; without this, "scan to
    pair" has no path to becoming real, no matter how complete the sync transport underneath it
    gets.
  ],
  related: (("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)]), ("PL-025", [Multi-Device Sync Service])),
)
