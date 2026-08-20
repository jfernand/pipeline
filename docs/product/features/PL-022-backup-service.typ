#import "../template.typ": feature

#feature(
  designator: "PL-022",
  name: "Backup Service",
  status: "Planned",
  summary: [
    A background service on desktop. Announces itself on the local network. Other devices back
    themselves up to it on their own schedule — daily by default. Scheduling gets a UI; discovery
    does not need one.
  ],
  purpose: [
    Local-first (PL-004, PL-011) means the data is only as safe as the one device it lives on.
    This is the backup PL-014's export/import can't be on its own: automatic, scheduled, running
    whether or not anyone remembers to do it by hand.
  ],
  implementation: (),
  related: (
    ("PL-004", [Local Application Database]),
    ("PL-011", [Event-Sourced Local Storage]),
    ("PL-014", [Export / Import Applications]),
    ("PL-025", [Multi-Device Sync Service]),
  ),
)
