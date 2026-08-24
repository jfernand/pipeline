#import "../template.typ": feature

#feature(
  designator: "PL-037",
  name: "Artifact Version from Build Info",
  status: "Planned",
  release: "MVP",
  summary: [
    The version baked into each shipped artifact — Android's `versionName`/`versionCode`, the
    desktop package's `packageVersion` — set from the same `git describe` output the NavRail
    already shows, instead of hand-maintained numbers that drift from it.
  ],
  purpose: [
    Right now there are three different "version"s in play, none of them agreeing: the NavRail
    reads live `git describe` output; Settings' "About" row hardcodes a string that's already
    stale; and the actual installable artifacts (`androidApp`'s `versionCode`/`versionName`,
    `desktopApp`'s `packageVersion`) are hardcoded numbers that have never moved since `1.0`/`1.0.0`.
    What a user sees in the app and what's stamped on the artifact they installed should be the
    same answer to "what version is this."
  ],
  related: (("PL-001", [Design System Foundation]), ("PL-012", [App Preferences & Developer Mode])),
)
