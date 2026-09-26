#import "../template.typ": feature

#feature(
  designator: "PL-041",
  name: "Routes and Deep Links",
  status: "Planned",
  release: "MVP",
  summary: [
    A fuller navigation and deep-link schema — routes that carry the state a screen is actually
    showing (the list's active status filter, its search text), not just which screen and which
    application id — and every route reachable through MCP, not only `DetailRoute` via
    `open_application`. Of the app's eight routes (`Routes.kt`), one — `DetailRoute` — is
    deep-linkable today. The other seven aren't: `ListRoute`, `AddEditRoute`, `FollowUpsRoute`,
    `SyncRoute`, `SettingsRoute`, `PairRoute`, and `DevToolsRoute` all require in-app navigation to
    reach, with no `pipeline://` link and no MCP tool for any of them.
  ],
  purpose: [
    `ListRoute` is a bare object today; the list screen's filter and search state
    (`rememberApplicationListFilter`) lives in local Compose state that a route, a deep link, or an
    MCP call has no way to see or set. Landing on "Rejected applications matching 'acme'" — from a
    saved link, a notification, or an agent — isn't possible; the best any of those can do is open
    the unfiltered list and leave the user to redo the filtering by hand. And of the app's eight
    routes (`Routes.kt`), only `DetailRoute` has an MCP tool that reaches it (`open_application`,
    PL-017) — everything else PL-041 would add stays UI-only unless every route gets the same
    treatment PL-017 gave one of them.

    Confirmed missing, one at a time, while shooting the user manual's screenshots (PL-026): every
    shot that wasn't the plain detail screen needed a live click through the UI, because no link or
    MCP call could get there directly — the list itself (`ListRoute`, with a specific filter/search
    already applied), Settings (`SettingsRoute`), Follow-ups (`FollowUpsRoute`), Dev Tools
    (`DevToolsRoute`), and Sync/pairing (`SyncRoute`, `PairRoute`). The New/Edit application form
    (`AddEditRoute`) is a route already, just not a deep-linkable one. The Update-status sheet,
    Add-contact sheet, and delete-confirmation sheet aren't routes at all — they're transient
    overlay state on top of `DetailRoute` — so reaching one by link means carrying a sub-state
    alongside the id, not adding a ninth route.
  ],
  related: (
    ("PL-009", [In-App Navigation & Deep Links]),
    ("PL-008", [Search & Filter Applications]),
    ("PL-013", [MCP Server Infrastructure]),
    ("PL-017", [Open Application (MCP Deep-Link Navigation)]),
    ("PL-026", [User Manual]),
  ),
)
