#import "../template.typ": feature

#feature(
  designator: "PL-039",
  name: "Platform App Icons",
  status: "Planned",
  release: "MVP",
  summary: [
    A real app icon for every platform — Android launcher, iOS `AppIcon`, desktop window/dock
    icon, web favicon — built from the same "PL" monogram (Barlow Condensed, brand amber on the
    dark ground) `NavRail` already uses as Pipeline's mark.
  ],
  purpose: [
    Right now Android ships the stock Android Studio template icon (`ic_launcher_background`/
    `ic_launcher_foreground`), iOS's `AppIcon.appiconset` is the empty Xcode default, and desktop
    and web have no icon configured at all. None of the four platforms show anything a user would
    recognize as Pipeline before the app has even opened — the one place that identity already
    exists is `NavRail`'s own "PL" mark, once you're already inside the app.
  ],
  related: (("PL-001", [Design System Foundation]),),
)
