#import "../template.typ": feature

#feature(
  designator: "PL-012",
  name: "App Preferences & Developer Mode",
  status: "Shipped",
  release: "MVP",
  summary: [
    A Settings screen, backed by a per-device `PreferencesStore` — sync network mode, developer
    mode. Developer mode reveals a Dev Tools nav rail item on tablet. Hidden by default.
  ],
  purpose: [
    One place to control device-level behavior. The MCP server toggle (PL-013) and export/import
    (PL-014) are rows on this screen — not their own.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/PreferencesStore.kt, AppPreferences.kt — the contract",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/SettingsPreferencesStore.kt — multiplatform-settings-backed implementation",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/SettingsScreen.kt — the screen",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/DevToolsContent.kt, ui/nav/Routes.kt — DevToolsRoute, gated on developerMode",
  ),
  related: (("PL-013", [MCP Server Infrastructure]), ("PL-014", [Export / Import Applications]), ("PL-016", [List Settings (MCP)])),
)
