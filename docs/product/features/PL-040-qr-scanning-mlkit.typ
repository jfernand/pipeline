#import "../template.typ": feature

#feature(
  designator: "PL-040",
  name: "QR Scanning (ML Kit)",
  status: "Planned",
  summary: [
    Camera-based scanning of the sync-key QR code PL-038 generates, using ML Kit's barcode
    scanning — the other half of pairing: PL-038 makes the code real to display, this makes it
    real to read.
  ],
  purpose: [
    Pairing a second device means one side shows the code (PL-038) and the other side reads it.
    Without a real scanner, "scan to pair" has no way to actually complete the handshake, no
    matter how real the displayed code or the sync transport underneath it become. ML Kit's
    on-device barcode scanning avoids sending camera frames anywhere off-device — consistent with
    the "no account, no server, nothing in between" pairing already promises.
  ],
  related: (("PL-038", [Sync Key QR Generation]), ("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)])),
)
