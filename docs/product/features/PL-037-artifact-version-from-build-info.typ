#import "../template.typ": feature

#feature(
  designator: "PL-037",
  name: "Artifact Version from Build Info",
  status: "Shipped",
  release: "MVP",
  summary: [
    The version baked into each shipped artifact — Android's `versionName`/`versionCode`, the
    desktop package's `packageVersion` — is now set from the same `git describe` output the
    NavRail and Settings' "About" row already show at runtime, instead of hand-maintained numbers
    that had never moved since `1.0`/`1.0.0`.
  ],
  purpose: [
    NavRail and Settings' "About" row already read live `git describe` output
    (`BuildInfo.GIT_DESCRIBE`), but the actual installable artifacts — `androidApp`'s
    `versionCode`/`versionName`, `desktopApp`'s `packageVersion` — were still hardcoded numbers
    that had never moved. What a user sees in the app and what's stamped on the artifact they
    installed should be the same answer to "what version is this."
  ],
  implementation: (
    "androidApp/build.gradle.kts — versionCode from commit count, versionName from git describe (identical to BuildInfo.GIT_DESCRIBE)",
    "desktopApp/build.gradle.kts — packageVersion as 1.0.<commit count>; MSI's strict major.minor.patch requirement rules out the raw describe string Android's free-form versionName can use directly",
  ),
  related: (("PL-001", [Design System Foundation]), ("PL-012", [App Preferences & Developer Mode])),
)
