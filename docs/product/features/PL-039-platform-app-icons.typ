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
  description: [
    Linux needed more than `Window(icon = ...)`. That call sets the window's icon via
    `Window.setIconImage()`, which on this Linux/XWayland setup never lands in `_NET_WM_ICON` (the
    property is simply absent — confirmed against a real running instance) — so GNOME's dock
    (while the app is running) and Alt+Tab switcher both fell back to a generic icon, even though
    the *not-running* pinned dock entry showed Pipeline's mark correctly (that one reads the
    `.desktop` file's `Icon=` directly, a different code path). `main.kt` now also calls
    `java.awt.Taskbar.setIconImage(...)` — the API actually meant for "the icon the OS chrome
    shows for this running app" — which does land in `_NET_WM_ICON`. `packageReleaseDeb` also
    patches a `StartupWMClass` into the generated `.desktop` file, for window managers that prefer
    app-identity matching over a window's own icon; getting that value right took checking every
    X11 window the process opens individually; the one `xdotool`'s class search finds first isn't
    the real visible window; the real one only reveals itself by matching its title.
  ],
  implementation: (
    "androidApp/src/main/res/drawable/ic_launcher_background.xml, ic_launcher_foreground.png — adaptive icon layers; legacy mipmap-*dpi/ic_launcher(_round).png cover pre-API26",
    "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png — Xcode 14+ single-size universal icon",
    "desktopApp/icon/ (icon.icns/.ico/.png) — compose.desktop.application.nativeDistributions per-OS packaged icon",
    "desktopApp/src/main/resources/icon.png, main.kt — the window's own titlebar icon, plus Taskbar.setIconImage for the running-app icon on Linux",
    "desktopApp/build.gradle.kts — packageReleaseDeb patches StartupWMClass into the generated .desktop file",
    "webApp/src/webMain/resources/favicon.ico, favicon.png, index.html — browser tab icon",
  ),
  related: (("PL-001", [Design System Foundation]),),
)
