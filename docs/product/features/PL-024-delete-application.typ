#import "../template.typ": feature

#feature(
  designator: "PL-024",
  name: "Delete Application",
  status: "Planned",
  release: "Backlog",
  summary: [
    Removes an application outright. Slide to reveal a trash can on phone, a context menu where
    the platform supports one, the Delete key on desktop.
  ],
  purpose: [
    Every application PL-006 can create currently outlives its usefulness with no way back out —
    a mis-added entry, a withdrawn application, sits in the list forever. Add and Edit have always
    had a counterpart for undoing themselves. Delete has not, until this.
  ],
  description: [
    A soft delete, not a hard one — consistent with PL-042 making the event log the sole source of
    truth. A new `ApplicationDeleted` event marks `ApplicationState.deleted = true`; nothing is
    removed from `event_envelopes`, so the full history an application had survives even after it
    stops showing up anywhere. List and search (PL-008) filter deleted applications out; detail
    (PL-002) and the MCP tools treat a deleted id the same way an unknown one is treated today.
  ],
  implementation: (),
  related: (
    ("PL-006", [Add Application]),
    ("PL-004", [Local Application Database]),
    ("PL-042", [Event Log as Source of Truth]),
  ),
)
