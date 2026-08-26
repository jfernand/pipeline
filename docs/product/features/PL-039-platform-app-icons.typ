#import "../template.typ": feature

#feature(
  designator: "PL-039",
  name: "Platform App Icons",
  status: "Shipped",
  release: "MVP",
  summary: [
    A real app icon for every platform — Android launcher, iOS `AppIcon`, desktop window/dock
    icon, web favicon — built from the same "PL" monogram (Barlow Condensed ExtraBold, brand
    amber on `PlColors.bgBase`) `NavRail` already uses as Pipeline's mark.
  ],
  purpose: [
    Android shipped the stock Android Studio template icon (`ic_launcher_background`/
    `ic_launcher_foreground` — the green robot grid), iOS's `AppIcon.appiconset` carried the
    Kotlin Multiplatform wizard's default (a blue hexagon), and desktop and web had no icon
    configured at all. None of the four platforms showed anything a user would recognize as
    Pipeline before the app had even opened — the one place that identity already existed was
    `NavRail`'s own "PL" mark, once you were already inside the app.
  ],
  implementation: (
    "androidApp/src/main/res/drawable/ic_launcher_background.xml, ic_launcher_foreground.png — adaptive icon layers; legacy mipmap-*dpi/ic_launcher(_round).png cover pre-API26",
    "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png — Xcode 14+ single-size universal icon",
    "desktopApp/icon/ (icon.icns/.ico/.png) — compose.desktop.application.nativeDistributions per-OS packaged icon",
    "desktopApp/src/main/resources/icon.png, main.kt — the running window's own titlebar/dock icon, separate from the packaged installer's",
    "webApp/src/webMain/resources/favicon.ico, favicon.png, index.html — browser tab icon",
  ),
  related: (("PL-001", [Design System Foundation]),),
)
