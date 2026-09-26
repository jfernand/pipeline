#import "../template.typ": feature

#feature(
  designator: "PL-041",
  name: "Routes and Deep Links",
  status: "Shipped",
  release: "MVP",
  summary: [
    A fuller navigation and deep-link schema — routes that carry the state a screen is actually
    showing (the list's active status filter, its search text), not just which screen and which
    application id — and every route reachable through MCP, not only `DetailRoute` via
    `open_application`. Both halves are done: every one of the app's eight routes, and all three
    of the detail screen's sheets, opens from a `pipeline://` link (PL-009), and the `open_link`
    MCP tool opens any of those links in the running app.
  ],
  purpose: [
    `ListRoute` used to be a bare object; the list screen's filter and search state
    (`rememberApplicationListFilter`) lived in local Compose state that a route, a deep link, or an
    MCP call had no way to see or set. Landing on "Rejected applications matching 'acme'" — from a
    saved link, a notification, or an agent — wasn't possible; the best any of those could do was
    open the unfiltered list and leave the user to redo the filtering by hand. And of the app's eight
    routes (`Routes.kt`), only `DetailRoute` has an MCP tool that reaches it (`open_application`,
    PL-017) — everything else PL-041 would add stays UI-only unless every route gets the same
    treatment PL-017 gave one of them.

    Confirmed missing, one at a time, while shooting the user manual's screenshots (PL-026): every
    shot that wasn't the plain detail screen needed a live click through the UI, because no link or
    MCP call could get there directly — the list itself (`ListRoute`, with a specific filter/search
    already applied), Settings (`SettingsRoute`), Follow-ups (`FollowUpsRoute`), Dev Tools
    (`DevToolsRoute`), and Sync/pairing (`SyncRoute`, `PairRoute`). The New/Edit application form
    (`AddEditRoute`) was a route already, just not a deep-linkable one. The Update-status sheet,
    Add-contact sheet, and delete-confirmation sheet aren't routes at all — they're transient
    overlay state on top of `DetailRoute` — so reaching one by link means carrying a sub-state
    alongside the id, not adding a ninth route: `DetailRoute.sheet`, read once on arrival.

    Shipped in PL-009, 2026-09-26: `list` with its `?q=&status=&exclude=` filters, `followups`,
    `app/new`, `app/{id}/edit`, the three `app/{id}?sheet=` sheets, `settings`, `settings/pair`,
    `sync` and `devtools`. The user manual's chapter 9 lists them all.

    The MCP half is one tool, `open_link`, not a tool per route: it takes a `pipeline://` link and
    pushes it onto `DeepLinkBus`, the path `open_application` (PL-017) already takes, so an agent
    reaches exactly what a link reaches and the route table lives in one place — the nav graphs.
    The one check it adds is the link's destination, because a link the graph doesn't recognize is
    dropped without a word; checking first turns that into an error the agent actually sees.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/nav/Routes.kt — ListRoute's q/status/exclude, DetailRoute's sheet, DetailSheet",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/PipelineApp.kt, ui/phone/PipelinePhoneApp.kt — every route's deep links; sheet-on-arrival; devtools fallback",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/StatusFilterChips.kt — parseStatusFilters, initial filter state",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/screens/AddEditScreen.kt — closes on an unknown or deleted id",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — open_link MCP tool",
    "androidApp/src/main/AndroidManifest.xml — intent filter hosts",
  ),
  related: (
    ("PL-009", [In-App Navigation & Deep Links]),
    ("PL-008", [Search & Filter Applications]),
    ("PL-013", [MCP Server Infrastructure]),
    ("PL-017", [Open Application (MCP Deep-Link Navigation)]),
    ("PL-026", [User Manual]),
  ),
)
