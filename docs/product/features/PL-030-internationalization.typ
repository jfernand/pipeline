#import "../template.typ": feature

#feature(
  designator: "PL-030",
  name: "Internationalization",
  status: "Planned",
  summary: [
    Every string in the app, in more than one language. Not just translation — date formats,
    pluralization, right-to-left layout where it applies.
  ],
  purpose: [
    Every hardcoded English string is a decision the next translation has to work around instead
    of through. The later this starts, the more of those decisions there are to undo.
  ],
  implementation: (),
  related: (("PL-001", [Design System Foundation]),),
)
