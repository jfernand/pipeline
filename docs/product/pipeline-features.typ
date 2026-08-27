#import "isss-doc.typ": *
#import "template.typ": feature, release-badge, status-stamp
#import "@preview/cetz:0.4.2": canvas, draw

#show: isss-doc.with(
  title: "Pipeline",
  subtitle: "Feature catalog — shipped and planned capabilities.",
  class: "Product Reference",
  doc-id: "ISSS-0001",
  revision: "1.46",
  date: "2026-08-26",
  status: "Current",
  applies-to: "Pipeline — Android, iOS, Desktop, Web",
  owner: "Javier Fernández",
  classification: "",
)

#heading(level: 1, numbering: none)[About This Catalog]

== Mission

Pipeline tracks job applications. Company, role, status, dates, contacts, follow-ups. Nothing
else, and nothing less.

It also runs an MCP server, and that is not a bolt-on. An agent reads and writes the same data the
UI does — not a copy, not an export. The same pipeline, through a different door.

== Conventions

- Every feature gets a designator, PL-NNN, the moment it ships or serious work starts on it. That
  number is permanent. It does not get renumbered, and it does not get reused — not even after the
  feature is gone. Use it in commits, in PRs, in conversation.
#note[Numbers run in shipping order. PL-001 shipped first, PL-017 last. That is all they mean.]

- One file per feature: `features/PL-NNN-slug.typ`, built from `feature()` in `template.typ`. Every
  entry has the same four parts — status, summary, purpose, implementation — because a reference
  you have to relearn the shape of each time is not a reference.
- Related designators live in the margin, as cross-references. Not a bulleted list fighting the
  body text for the reader's eye.
- Known rough edges in shipped features are written once, in `issues.typ`, and rendered together
  on the Known Issues page at the end. A feature page never carries its own copy of the text —
  just a short linked pointer to the matching entries there, and each of those links back.

== Revision History

#data-table(
  columns: (auto, auto, 1fr),
  header: ("Rev", "Date", "Changes"),
  [1.0], [2026-08-19], [Initial catalog: PL-001 through PL-031, in four parts — Core Application,
    Local Storage/Portability/Sync, MCP Integration, Services.],
  [1.1], [2026-08-20], [Known rough edges in shipped features consolidated into a single Known
    Issues page, cross-linked to the feature(s) each belongs to. Added PL-032, Editability
    Affordance Language, as a new planned feature.],
  [1.2], [2026-08-22], [Fixed: notes on PL-002's detail screen are editable in place, using
    PL-032's corner-marker affordance — closes the "Notes aren't editable" entry on the Known
    Issues page.],
  [1.3], [2026-08-22], [Fixed: Return-to-commit on the Notes field didn't work from an Android
    on-screen keyboard — it inserts a newline directly, without dispatching the `KeyEvent` the
    fix relied on. Now also commits via the field's IME "Done" action, which the soft keyboard
    does send.],
  [1.4], [2026-08-22], [Fixed: editable affordances had no cancel path — closes "No
    Escape-to-cancel" on the Known Issues page. Escape (hardware keyboards) and a ✕ button next
    to the Notes field (all platforms) both discard the draft without saving. The button is
    marked non-focusable — otherwise clicking it blurred and committed the field before the
    button's own click ever ran.],
  [1.5], [2026-08-22], [Every entry on the Known Issues page now carries a stable PL-XXXX-YYY
    number — XXXX the issue's primary feature, YYY its sequence within that feature — instead of
    being identified only by title.],
  [1.6], [2026-08-22], [Added PL-033, Event Provenance, as a new planned feature: recording which
    device or AI/MCP client made each event-log change, not just what changed.],
  [1.7], [2026-08-22], [Fixed: the Source/Added line on the detail screen (phone and tablet) was
    one combined string prone to wrapping mid-line on narrow widths — closes "'Applied' line
    wraps" on the Known Issues page. Now a structured two-line block; tablet also gained the
    "Added" date it was previously missing entirely.],
  [1.8], [2026-08-22], [Extended the same fix to the application list: `AppCard` previously
    dropped the job source entirely — it now shows a small icon for it (Referral, LinkedIn,
    Company site, Recruiter, Other), with the full text still on the detail screen.],
  [1.9], [2026-08-22], [Added PL-034, Event Provenance Indicator, as a new planned feature: a
    small human/AI icon on the list card, alongside the job-source one, showing who or what made
    an application's most recent change. Depends on PL-033 for the underlying data.],
  [1.10], [2026-08-23], [Fixed: the Dev Tools event log capitalized payload text — closes "Dev log
    capitalizes payloads" on the Known Issues page. `MonoText` gained an `uppercase` opt-out for
    text whose casing is data, not a label; the event payload is the first (and only) caller to
    use it.],
  [1.11], [2026-08-23], [The Dev Tools event log now pretty-prints each event's JSON payload for
    display, instead of the compact single-line form it's stored and hashed as. Display-only —
    the stored envelope payload is untouched, since it's part of the hash input that gives each
    event its identity.],
  [1.12], [2026-08-23], [Added PL-035, Opening Animation, as a new planned feature: promoted from
    the "Add animation" entry on the Known Issues page — a shared-element/shared-outline
    transition for opening an application belongs as a feature in its own right, not a rough edge
    of PL-009.],
  [1.13], [2026-08-23], [Fixed: the "MCP server" toggle and "Address" rows on Settings were
    separate — closes "MCP toggle/address should merge" on the Known Issues page. Now one row,
    the port editable inline. A port change now actually restarts the running server on the new
    port too — previously `start()` no-oped whenever a server was already running, regardless of
    which port it was asked for, and the effect that calls it wasn't even keyed on the port to
    begin with.],
  [1.14], [2026-08-23], [Fixed: the Dev Tools event log rendered oldest-first, newest entry at the
    bottom — closes "Dev log entries should be reverse order" on the Known Issues page. Now
    reversed for display; the underlying chain stays in append order, since that's what the hash
    chain and sync's diffChains rely on.],
  [1.15], [2026-08-23], [Added PL-036, Advanced Filter Dialog, as a new planned feature: promoted
    from the "No advanced filter dialog" entry on the Known Issues page — multi-field filtering
    belongs as a feature in its own right, not a rough edge of PL-008.],
  [1.16], [2026-08-23], [Split Sync out of the old "Local Storage, Data Portability & Data Sync"
    part into its own: PL-003 (Device Pairing & Multi-Device Sync UI) moves from Core Application,
    PL-025 (Multi-Device Sync Service) moves from Services — both were sync features filed under
    parts that otherwise had nothing to do with syncing.],
  [1.17], [2026-08-23], [Every part now opens with an intro explaining what it's for and the
    design principles behind it — also gives each part a proper name in the table of contents,
    where it previously didn't appear at all.],
  [1.18], [2026-08-23], [Added a Feature Index appendix — every PL, its status, and its page,
    driven off each feature's own designator/name/status rather than a second hand-kept list.],
  [1.19], [2026-08-23], [Fixed: Date applied and Next action were freehand ISO-8601 text fields —
    closes "Date applied has no picker" on the Known Issues page. Both now open a real
    `DatePickerDialog` via a new `DateField` component; converts through epoch days rather than
    local-timezone millis, avoiding the usual date-picker off-by-one.],
  [1.20], [2026-08-24], [Fixed: the deep-link `<intent-filter>` (PL-009) had `android:autoVerify`
    on a custom `pipeline://` scheme, which can't be verified — autoVerify requires http(s). Split
    into two filters: the unverified custom scheme as before, plus a real Android App Link for
    https://pipeline.casaroja.es with autoVerify. The in-app nav deep link now recognizes that URL
    too, not just pipeline://app, so an App Link actually routes to the right screen instead of
    just opening the app.],
  [1.21], [2026-08-24], [Fixed: Sync and Dev Tools were unreachable on phone-width screens —
    closes "Sync and Dev Tools unreachable on phone" on the Known Issues page. `PipelinePhoneApp`
    now wires `SyncRoute`/`DevToolsRoute` too, reached from Settings (the sync status card; a
    newly-navigable "Event log" row). `TabletSyncContent` — now shared by both, despite the name —
    stacks vertically below 640dp instead of a fixed-width side-by-side `Row` that would've
    overflowed a phone screen.],
  [1.22], [2026-08-24], [Added PL-037, Artifact Version from Build Info, as a new planned feature:
    the version stamped on each shipped artifact (Android's versionName/versionCode, the desktop
    package's packageVersion) should come from the same git describe output the NavRail already
    shows, instead of hand-maintained numbers that have drifted from it since 1.0.],
  [1.23], [2026-08-24], [Added PL-038, Sync Key QR Generation, as a new planned feature: a real,
    scannable QR code encoding the iroh ticket PL-025 generates, replacing QrCodePlaceholder's
    deterministic pseudo-QR pattern, which looks like a code but decodes to nothing.],
  [1.24], [2026-08-24], [Fixed two: filter chips on the phone list screen ran off the edge of a
    portrait screen instead of wrapping — closes "Filter chips run off screen on portrait phone"
    on the Known Issues page — now a `FlowRow`, matching tablet's `ListPane.kt`. And Settings'
    "About" row showed a hardcoded "Pipeline 1.4.0" — closes "About box shows a hardcoded
    version" — now reads the same `BuildInfo.GIT_DESCRIBE` the NavRail already does.],
  [1.25], [2026-08-24], [Added PL-039, Platform App Icons, as a new planned feature: a real icon
    for every platform, built from the same "PL" monogram NavRail already uses — Android currently
    ships the stock Android Studio template icon, iOS's AppIcon.appiconset is the empty Xcode
    default, and desktop/web have no icon configured at all.],
  [1.26], [2026-08-24], [Added PL-040, QR Scanning (ML Kit), as a new planned feature: camera-based
    scanning of PL-038's sync-key QR code, on-device via ML Kit — the read half of pairing to
    PL-038's display half. No camera frames leave the device, consistent with pairing's existing
    "no account, no server, nothing in between" promise.],
  [1.27], [2026-08-24], [Added a `release` field to `feature()` — a small filled badge next to
    the status stamp, and a new Release column in the Feature Index, driven off the same
    source-of-truth metadata as everything else there. Every Shipped feature (PL-001–002,
    004–009, 011–017) is tagged `"MVP"`; so are eleven Planned ones targeted for that same
    release: PL-010, 018, 019, 021, 023, 026, 031, 033, 034, 037, 039.],
  [1.28], [2026-08-24], [PL-032, Editability Affordance Language, marked Shipped — its
    `EditableAffordanceBox` is already live in two real call sites (`NotesSection`, the MCP port
    field in `SettingsScreen.kt`), not just the design exploration. Tagged `"MVP"` too, following
    from rev 1.27's "every Shipped feature belongs to the MVP release" rule applied to a feature
    that became Shipped after that rule was set.],
  [1.29], [2026-08-24], [Fixed: the Feature Index's Status column was plain text — "Shipped" and
    "Planned" read identically at a glance. Now reuses `status-stamp()`, the same outlined pill
    each feature's own page already renders, so Planned is visually distinct there too.],
  [1.30], [2026-08-24], [Fixed: `feature()`'s `description` parameter defaulted to `[]` (empty
    content), not `none` — `description != none` was always true, so every feature page rendered
    an empty "Description" heading over nothing except the one entry (PL-031) that actually passed
    one. Default is `none` now; the heading only appears when there's something under it.],
  [1.31], [2026-08-24], [Added a Navigation Routes appendix — every route in `Routes.kt`, which
    screen it opens on phone and on tablet, and its deep link if it has one. Hand-maintained, not
    derived from code the way the Feature Index is, so it can drift if a route changes without a
    matching edit here.],
  [1.32], [2026-08-24], [Added an MCP Tool row to the Navigation Routes appendix. Only
    `open_application` actually navigates anywhere — it pushes a deep link onto `DeepLinkBus`,
    landing on `DetailRoute` the same way a real `pipeline://` link would; the other four tools
    (`add_application`, `edit_application`, `list_applications`, `list_settings`) read or write
    data through the repository directly and never touch a route.],
  [1.33], [2026-08-24], [Added PL-041, Routes and Deep Links, as a new planned feature (MVP):
    flesh out the routing/deep-link schema to carry state beyond "which screen, which
    application" — the list screen's active status filter and search text, notably — and give
    every route an MCP tool, not just `DetailRoute`'s `open_application`.],
  [1.34], [2026-08-24], [Tagged every feature in Part 3, Sync, `"MVP"`: PL-003, PL-025, PL-038,
    PL-040.],
  [1.35], [2026-08-24], [`release` gets its second value: `"Backlog"`, tagged on PL-020, PL-024,
    PL-027, PL-036 — same badge as `"MVP"`, distinguished only by label text for now.],
  [1.36], [2026-08-24], [`release` gets a third value: `"1.1"`, tagged on PL-022, PL-028, PL-030.
    PL-029 tagged `"MVP"`; PL-035 tagged `"Backlog"`.],
  [1.37], [2026-08-24], [Fixed: `OverdueBadge` sat as a same-size badge beside `StatusChip` in
    `AppCard` — closes "Overdue badge alignment" on the Known Issues page. Smaller now, and
    positioned as a tab overlapping the chip's top-right corner instead of a row sibling occupying
    its own space.],
  [1.38], [2026-08-25], [Shipped PL-033, Event Provenance: every `ApplicationEvent` now carries an
    `EventProvenance` — `Device` for an ordinary in-app edit, `McpClient` for one made through the
    MCP server, or `Unknown`, the default for event payloads written before this field existed, so
    old event logs keep reading correctly.],
  [1.39], [2026-08-25], [`EventProvenance.Device` now carries the device's actual `DeviceId`,
    instead of leaving it implicit. That id is standardized across every platform: `DeviceIdentityStore`
    moved off a Room table only Android/JVM/iOS had, onto an `expect fun createDeviceIdentityStore()`
    backed by that platform's `Settings` — android, ios, jvm, js, and wasmJs each get an actual, so
    every target generates and persists the id the same way.],
  [1.40], [2026-08-25], [The event log (PL-011) is now the sole source of truth: the Room cache
    tables (`applications`, `status_events`, `contacts`, `reminders`) are gone, replaced by
    `InMemoryApplicationStateStore` on every platform, materialized by replaying `event_envelopes`
    through a new `replayApplicationState` on first access. A schema change to how state is shaped
    no longer needs a database migration — only the event types staying additive does, the same
    discipline PL-033 already established. Fixes real data loss on js/wasmJs (previously reset on
    every reload) and a latent bug where an edit's `applicationId` didn't match its application's
    original create event, which would have silently broken replay grouping.],
  [1.41], [2026-08-25], [Added and shipped PL-042, Event Log as Source of Truth, formalizing 1.40's
    change in the catalog.],
  [1.42], [2026-08-25], [Shipped PL-037, Artifact Version from Build Info: `androidApp`'s
    `versionCode`/`versionName` and `desktopApp`'s `packageVersion` now derive from git (commit
    count, `git describe`) instead of numbers hand-set once and never touched again.],
  [1.43], [2026-08-25], [Shipped PL-039, Platform App Icons: Android, iOS, desktop, and web all
    now show the "PL" monogram instead of template defaults — the stock Android Studio robot, the
    Kotlin Multiplatform wizard's hexagon, and no icon at all on desktop and web.],
  [1.44], [2026-08-25], [Shipped PL-023, Negative Search Filters: every status filter chip now
    cycles not-set → required → excluded independently, replacing the old single-select filter —
    `StatusFilterChips`' state is a `Map<AppStatus, StatusFilterMode>` now, not a single
    `AppStatus?`. `PlFilterChip` gained a `negative` variant (`PlColors.danger`, distinct from
    `active`'s brand amber) to render it.],
  [1.45], [2026-08-26], [Shipped PL-010, Follow-ups: the tablet nav tab behind
    `PlaceholderPane("Follow-ups")` now lists every overdue next-action date and overdue reminder
    across the whole pipeline, sorted by due date — `JobApplicationRepository.observeFollowUps`,
    backed by a new `toFollowUpItems` mapping over every application's live state.],
  [1.46], [2026-08-26], [Shipped PL-019, Fake Data Mode: a "Show fake data" switch in Dev Tools
    now reads and writes a separate demo event chain (`EventLogKind`), seeded once from
    `SeedData` as real `ApplicationCreated` events rather than synthesized into memory. Closes
    this catalog's own review's top finding — an empty log is genuinely empty now, replacing the
    old showingSeedData special-case in `InMemoryApplicationStateStore` that let an edit to
    ephemeral demo data silently replay to nothing on restart.],
)

#part(1, "Core Application",
  blurb: [What the app is for. Everything a user opens Pipeline to do, before any of it talks to
    an agent.])

#heading(level: 2, numbering: none)[Core Application]

This is the whole reason to open Pipeline: one place a job search lives, instead of a spreadsheet,
a pile of bookmarked postings, and whatever a memory holds onto. Browse, add, edit, filter, delete
— the complete set of things a user does to the data themselves, with nothing here waiting on a
network to respond.

Two principles hold the part together. First, one shared design system (PL-001) and one shared
list/detail structure across phone and tablet — a user who's learned one screen has learned the
shape of all of them, not a new layout to decode per feature. Second, small edits happen in place:
an editable affordance (PL-032) that turns text into a field on tap, not a separate edit-mode
dialog for every field that might change.

#include "features/PL-001-design-system-foundation.typ"
#include "features/PL-002-browse-applications.typ"
#include "features/PL-004-local-application-database.typ"
#include "features/PL-005-update-application-status.typ"
#include "features/PL-006-add-application.typ"
#include "features/PL-007-edit-application.typ"
#include "features/PL-008-search-filter-applications.typ"
#include "features/PL-009-in-app-navigation-deep-links.typ"
#include "features/PL-010-follow-ups.typ"
#include "features/PL-021-remember-last-route.typ"
#include "features/PL-023-negative-search-filters.typ"
#include "features/PL-024-delete-application.typ"
#include "features/PL-026-user-manual.typ"
#include "features/PL-027-app-password.typ"
#include "features/PL-028-calendar-integration.typ"
#include "features/PL-029-interview-events-notes.typ"
#include "features/PL-030-internationalization.typ"
#include "features/PL-032-editability-affordance-language.typ"
#include "features/PL-034-event-provenance-indicator.typ"
#include "features/PL-035-opening-animation.typ"
#include "features/PL-036-advanced-filter-dialog.typ"
#include "features/PL-037-artifact-version-from-build-info.typ"
#include "features/PL-039-platform-app-icons.typ"
#include "features/PL-041-routes-and-deep-links.typ"

#part(2, "Local Storage & \nData Portability",
  blurb: [What happens to the data after it is entered — where it lives, what the user controls,
    and how it gets out again.])

#heading(level: 2, numbering: none)[Local Storage & Data Portability]

Data stays on-device, stays yours, and stays whole — export it, import it, and it's the same data,
not a lossy snapshot. "No account. No server. No telemetry." isn't a footnote; it's the reason this
part exists at all.

Everything here reads or writes one thing: the event log (PL-011). Not the list view's current
snapshot, not a sync-specific copy — the actual append-only history everything else derives from,
including MCP's tools and, eventually, sync (see Part 3). Developer mode (PL-012) exposes that log
directly rather than hiding it, on the theory that a tool that won't show its own history isn't
one worth trusting with a job search.

#include "features/PL-011-event-sourced-local-storage.typ"
#include "features/PL-012-app-preferences-developer-mode.typ"
#include "features/PL-014-export-import-applications.typ"
#include "features/PL-018-document-attachments.typ"
#include "features/PL-019-fake-data-mode.typ"
#include "features/PL-020-file-picker-module.typ"
#include "features/PL-033-event-provenance.typ"
#include "features/PL-042-event-log-as-source-of-truth.typ"

#part(3, "Sync",
  blurb: [One pipeline, not a copy per device left to drift. Peer-to-peer, no server in the
    middle.])

#heading(level: 2, numbering: none)[Sync]

A job search happens across more than one device — phone in a waiting room, tablet at a desk — and
a second, unsynced copy on each is worse than no sync at all: two histories that quietly disagree.
This part is what keeps them the same pipeline.

It's built as a direct extension of Part 2, not a parallel system: sync moves the same event log
(PL-011) from one device to another, so what merges is exactly what was already the source of
truth, not a bespoke sync representation that could itself drift from it. Pairing is peer-to-peer
by design — a QR-carried key between two devices, no account and no server in the middle — the
same "no server" stance the data itself is held to.

#include "features/PL-003-device-pairing-sync-scaffold.typ"
#include "features/PL-025-multi-device-sync-service.typ"
#include "features/PL-038-sync-key-qr-generation.typ"
#include "features/PL-040-qr-scanning-mlkit.typ"

#part(4, "MCP Integration",
  blurb: [Same data, same actions, no separate API. An agent drives the pipeline the way a tap
    would.])

#heading(level: 2, numbering: none)[MCP Integration]

An AI assistant reads and writes the exact pipeline a human uses — the same repository, the same
event log — not a shadow copy reachable only through a bespoke integration API that has to be kept
in sync with the real one by hand.

The server binds to loopback only; nothing it does is reachable off the device it runs on, matching
the no-server stance the rest of the data model holds to. Every MCP tool call becomes a normal
event in PL-011's log, indistinguishable today from one a human made by tapping — which is exactly
the gap PL-033 and PL-034 exist to close.

#include "features/PL-013-mcp-server-infrastructure.typ"
#include "features/PL-015-list-applications.typ"
#include "features/PL-016-list-settings.typ"
#include "features/PL-017-open-application.typ"

#part(5, "Services",
  blurb: [Work the app does for itself — discovered on the network or run on a schedule, not
    opened by hand.])

#heading(level: 2, numbering: none)[Services]

Work that happens on a user's behalf without opening the app — backups, file management — so the
tool meant to cut overhead doesn't add its own. A service that needs supervision to keep working
isn't one; these run unattended, and Settings is where to check on one, not where one has to be
started by hand.

#include "features/PL-022-backup-service.typ"
#include "features/PL-031-file-management-service.typ"

#pagebreak(weak: true, to: "odd")
#include "known-issues.typ"

#pagebreak(weak: true, to: "odd")
#heading(level: 1, numbering: none)[Feature Index]

Every feature in this catalog, its current status, and the page it's on — in designator order,
not the shipping/reading order the parts above use.

#context {
  let entries = query(<feature-meta>).map(e => (
    designator: e.value.designator,
    name: e.value.name,
    status: e.value.status,
    release: e.value.at("release", default: none),
    loc: e.location(),
  ))
  let sorted = entries.sorted(key: e => int(e.designator.split("-").at(1)))
  data-table(
    columns: (auto, 1fr, auto, auto, auto),
    header: ("PL", "Name", "Status", "Release", "Page"),
    ..sorted
      .map(e => (
        link(e.loc)[#e.designator],
        e.name,
        status-stamp(e.status),
        if e.release != none { release-badge(e.release) } else { [] },
        link(e.loc)[#counter(page).at(e.loc).first()],
      ))
      .flatten(),
  )
}

#pagebreak(weak: true, to: "odd")
#heading(level: 1, numbering: none)[Navigation Routes]

Every destination `Routes.kt` declares — which screen it opens on phone and on tablet, its deep
link if it has one, and the MCP tool that reaches it, if any.

// A data-table with raw/code cells packed into narrow auto/1fr columns collided — Typst's table
// auto-sizing doesn't account for code spans' wrapping the way it does plain text, so cells
// overflowed and overlapped their neighbors instead of staying inside their column. This is the
// same fixed-label-column grid isss-doc.typ's own sidenote/xref already use for prose-plus-tag
// content, which doesn't hit that sizing problem.
#let route-row(route, phone, tablet, deep-link: none, mcp-tool: none) = block(
  below: 14pt,
  breakable: false,
  stroke: (bottom: rule-w + hairline),
  inset: (bottom: 10pt),
)[
  #text(font: mono-font, size: 10pt, weight: 600, fill: ink)[#route]
  #v(4pt)
  #grid(
    columns: (60pt, 1fr),
    column-gutter: 10pt,
    row-gutter: 4pt,
    label("Phone", size: 7pt), phone,
    label("Tablet", size: 7pt), tablet,
    ..if deep-link != none { (label("Deep Link", size: 7pt), deep-link) } else { () },
    ..if mcp-tool != none { (label("MCP Tool", size: 7pt), mcp-tool) } else { () },
  )
]

// Only open_application (PL-017) actually navigates anywhere — add_application, edit_application,
// list_applications and list_settings all read/write data through the repository directly and
// never touch a route, so they're not listed on any row here.
#route-row([ListRoute], [`ListScreen` — start destination], [`TabletListContent` — start destination])
#route-row(
  [DetailRoute(id: Long)],
  [`DetailScreen`],
  [`TabletDetailScreen`],
  deep-link: [`pipeline://app/{id}`; `https://pipeline.casaroja.es/app/{id}`],
  mcp-tool: [`open_application` — pushes `pipeline://app/{id}` onto `DeepLinkBus`, the same path a
    real deep link takes],
)
#route-row([AddEditRoute(id: Long? = null)], [`AddEditScreen`], [`AddEditScreen`])
#route-row([FollowUpsRoute], [Not registered — unreachable], [`FollowUpsContent`])
#route-row([SyncRoute], [`SyncScreen`], [`TabletSyncContent`])
#route-row([SettingsRoute], [`SettingsScreen`], [`SettingsScreen`])
#route-row([PairRoute], [`PairingScreen`], [`PairingScreen`])
#route-row([DevToolsRoute], [`DevToolsScreen`], [`DevToolsContent`])

`FollowUpsRoute` is the one route on this table with no phone entry point at all —
`PipelinePhoneApp`'s `NavHost` never registers it, unlike every other route here. Reachable on
tablet only, via `NavRail`'s "Follow-ups" tab.

#pagebreak(weak: true, to: "odd")
#heading(level: 1, numbering: none)[Data Model]

#let dm-box(pos, w, h, title, body, accent: amber, title-size: 7.3pt) = {
  import draw: *
  let (x, y) = pos
  rect((x, y), (x + w, y - h), fill: panel, stroke: rule-w + accent, radius: 3pt)
  content((x, y), (x + w, y - h))[
    #box(width: w * 1cm - 12pt)[
      #text(font: mono-font, size: title-size, weight: 600, fill: ink, tracking: 0.01em)[#title]
      #v(4pt)
      #text(font: mono-font, size: 6.1pt, fill: ink-second, tracking: 0.01em)[#body]
    ]
  ]
}

// Labelled at the arrow's midpoint rather than via content()'s own (from, to) bounding-box sizing
// — for a mostly-vertical arrow that bounding box is only as wide as the x-offset between the two
// points, which for a near-straight-down arrow is close to zero and wraps the label one word per
// line. An explicit width sidesteps that entirely.
#let dm-arrow(from, to, label: none, label-width: 3.6cm) = {
  import draw: *
  line(from, to, stroke: rule-w + ink-faint, mark: (end: ">", fill: ink-faint, scale: 0.45))
  if label != none {
    let mid = ((from.at(0) + to.at(0)) / 2, (from.at(1) + to.at(1)) / 2)
    content(mid)[
      #box(fill: paper-bg, inset: (x: 3pt, y: 1pt), width: label-width)[
        #align(center)[#text(font: mono-font, size: 5.5pt, fill: ink-faint, tracking: 0.01em)[#label]]
      ]
    ]
  }
}

== Applications

`ApplicationState` (`sync/event/ApplicationState.kt`) is the one entity in this app — a job
application, and the three small collections that belong to it. `StatusHistoryEntry`, `Contact`,
and `Reminder` aren't rows in their own table with a foreign key back — they have no id of their
own at all. Each lives entirely inside its application's own `ApplicationState`, held as a plain
nested list, replayed and discarded as one unit with it.

#v(4pt)

#align(center)[
  #canvas(length: 1cm, {
    import draw: *

    dm-box(
      (0.9, 0), 9.6, 2.6,
      [Application],
      [applicationId · company · role · status

        dateApplied · nextActionDate · postingUrl

        source · notes],
      accent: amber-deep,
    )

    dm-arrow((2.6, -2.6), (1.8, -4.6))
    dm-arrow((5.7, -2.6), (5.7, -4.6), label: [one Application, many of each — nested, not joined], label-width: 4cm)
    dm-arrow((8.8, -2.6), (9.6, -4.6))

    dm-box((0, -4.6), 3.6, 1.8, [StatusHistoryEntry], [status · date · note])
    dm-box((4.0, -4.6), 3.6, 1.8, [Contact], [name · role · email])
    dm-box((8.0, -4.6), 3.4, 1.8, [Reminder], [message · dueDate])
  })
]

#v(4pt)

`ApplicationDetail` (the detail screen) and `FollowUpItem` (PL-010) both read `Contact` and
`Reminder` at display time — `toApplicationDetail`, `toFollowUpItems` — but neither the app nor
the MCP server has a write path for either one yet: every `Contact` and `Reminder` on a real
application today only ever got there through `SeedData`. `AddEditScreen` and the event vocabulary
below cover every other field.

== Event Sourcing

One table, not several. `event_envelopes` (PL-011, PL-042) is the only thing any platform
persists — every shape below it, `ApplicationState` included, is a pure, in-process function of
what that table holds, recomputed at read time, never itself written to disk. Editing an
application doesn't update a row; it appends an event and re-folds.

#v(4pt)

#align(center)[
  #canvas(length: 1cm, {
    import draw: *

    // Row A — the event vocabulary (ApplicationEvent, sealed)
    dm-box((0, 0), 3.6, 1.5, [ApplicationCreated], [applicationId · input · provenance], title-size: 6.4pt)
    dm-box((4.0, 0), 3.6, 1.5, [ApplicationEdited], [applicationId · input · provenance], title-size: 6.4pt)
    dm-box((8.0, 0), 3.4, 1.5, [StatusChanged], [applicationId · status · note], title-size: 6.4pt)

    dm-arrow((1.8, -1.5), (4.6, -3.3), label: [append() —], label-width: 2.4cm)
    dm-arrow((5.8, -1.5), (5.7, -3.3), label: [hash-chained], label-width: 2.4cm)
    dm-arrow((9.7, -1.5), (6.8, -3.3), label: [per device], label-width: 2.4cm)

    // Row B — the one durable table
    dm-box(
      (0, -3.3), 11.4, 3.0,
      [event_envelopes — the only table],
      [id · hash · parentHashes · deviceId

        sequence · timestampEpochMillis

        payload · logKind (REAL | DEMO, PL-019)],
      accent: amber-deep,
    )

    dm-arrow((5.7, -6.3), (5.7, -8.3), label: [replay(): fold every event through applyEvent, per application], label-width: 4.6cm)

    // Row C — the in-memory projection every read starts from
    dm-box(
      (1.7, -8.3), 8.0, 2.8,
      [ApplicationState — in memory only],
      [applicationId · company · role · status

        dateApplied · nextActionDate · postingUrl · source · notes

        statusHistory[] · contacts[] · reminders[]],
    )

    dm-arrow((3.2, -11.1), (1.8, -13.1))
    dm-arrow((5.7, -11.1), (5.9, -13.1), label: [read-time projection], label-width: 3.6cm)
    dm-arrow((8.2, -11.1), (9.8, -13.1))

    // Row D — what each screen actually reads
    dm-box((0, -13.1), 3.6, 2.0, [JobApplication], [the list — toJobApplication])
    dm-box((4.0, -13.1), 3.6, 2.0, [ApplicationDetail], [the detail screen — toApplicationDetail])
    dm-box((8.0, -13.1), 3.4, 2.0, [FollowUpItem], [Follow-ups (PL-010) — toFollowUpItems])
  })
]

#v(4pt)

`logKind` is the only thing separating PL-019's demo sandbox from real data — one table holds two
independent hash-chained sequences, not two tables, so a schema change to one is a schema change
to both by construction. `ApplicationState` itself has no opinion on which chain it was replayed
from; `DemoSeedingEventLog` seeds an empty `DEMO` chain exactly once, and from then on it's
indistinguishable, to every layer above it, from a real one.
