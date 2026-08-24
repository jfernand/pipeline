#import "isss-doc.typ": *
#import "template.typ": feature, release-badge, status-stamp

#show: isss-doc.with(
  title: "Pipeline",
  subtitle: "Feature catalog — shipped and planned capabilities.",
  class: "Product Reference",
  doc-id: "ISSS-0001",
  revision: "1.29",
  date: "2026-08-24",
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
#include "features/PL-010-follow-ups-placeholder.typ"
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
