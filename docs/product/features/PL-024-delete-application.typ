#import "../template.typ": feature

#feature(
  designator: "PL-024",
  name: "Delete Application",
  status: "Planned",
  summary: [
    Removes an application outright. Slide to reveal a trash can on phone, a context menu where
    the platform supports one, the Delete key on desktop.
  ],
  purpose: [
    Every application PL-006 can create currently outlives its usefulness with no way back out —
    a mis-added entry, a withdrawn application, sits in the list forever. Add and Edit have always
    had a counterpart for undoing themselves. Delete has not, until this.
  ],
  implementation: (),
  related: (("PL-006", [Add Application]), ("PL-004", [Local Application Database])),
)
