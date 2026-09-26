#import "../template.typ": feature

#feature(
  designator: "PL-009",
  name: "In-App Navigation & Deep Links",
  status: "Shipped",
  release: "MVP",
  summary: [
    One type-safe `androidx.navigation` graph, shared by phone and tablet: List, Detail, Add/Edit,
    Settings, Pair. One deep-link scheme, four routes on it: `pipeline://app/{id}` for an
    application's detail screen, `pipeline://app/new` and `pipeline://app/{id}/edit` for the Add
    and Edit forms, and
    `pipeline://list` for the applications list, optionally pre-searched and filtered
    (`?q=…&status=…&exclude=…`) — Android intent filters, desktop CLI-arg handling, `xdg-open`
    registration on Linux. The OS can hand the app a link and land on any of them.
  ],
  purpose: [
    This is what makes "jump to application #8" possible: first as a link from the OS — a
    notification, a URL — and later as what PL-017 uses to do the same thing from an MCP client.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/nav/Routes.kt — type-safe routes",
    "shared/src/commonMain/kotlin/org/cr/pipeline/App.kt — shared NavHostController, initialDeepLink handling",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/PipelinePhoneApp.kt, ui/PipelineApp.kt — navDeepLink<DetailRoute>(basePath = \"pipeline://app\"), navDeepLink<ListRoute>(basePath = \"pipeline://list\"), navDeepLink { uriPattern = \"pipeline://app/new\" } and \"pipeline://app/{id}/edit\" on AddEditRoute",
    "androidApp/src/main/AndroidManifest.xml — intent filter for pipeline://app and pipeline://list",
    "desktopApp/src/main/kotlin/org/cr/pipeline/main.kt — cold-start deep link from CLI args",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/platform/linux/LinuxUrlSchemeRegistrationManager.kt — xdg-open scheme registration",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]), ("PL-017", [Open Application (MCP Deep-Link Navigation)])),
)
