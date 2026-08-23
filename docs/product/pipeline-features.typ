#import "isss-doc.typ": *
#import "template.typ": feature

#show: isss-doc.with(
  title: "Pipeline",
  subtitle: "Feature catalog — shipped and planned capabilities.",
  class: "Product Reference",
  doc-id: "ISSS-0001",
  revision: "1.14",
  date: "2026-08-23",
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
)

#part(1, "Core Application",
  blurb: [What the app is for. Everything a user opens Pipeline to do, before any of it talks to
    an agent.])

#include "features/PL-001-design-system-foundation.typ"
#include "features/PL-002-browse-applications.typ"
#include "features/PL-003-device-pairing-sync-scaffold.typ"
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

#part(2, "Local Storage, \nData Portability & Data Sync",
  blurb: [What happens to the data after it is entered — where it lives, what the user controls,
    and how it gets out again.])

#include "features/PL-011-event-sourced-local-storage.typ"
#include "features/PL-012-app-preferences-developer-mode.typ"
#include "features/PL-014-export-import-applications.typ"
#include "features/PL-018-document-attachments.typ"
#include "features/PL-019-fake-data-mode.typ"
#include "features/PL-020-file-picker-module.typ"
#include "features/PL-033-event-provenance.typ"

#part(3, "MCP Integration",
  blurb: [Same data, same actions, no separate API. An agent drives the pipeline the way a tap
    would.])

#include "features/PL-013-mcp-server-infrastructure.typ"
#include "features/PL-015-list-applications.typ"
#include "features/PL-016-list-settings.typ"
#include "features/PL-017-open-application.typ"

#part(4, "Services",
  blurb: [Work the app does for itself — discovered on the network or run on a schedule, not
    opened by hand.])

#include "features/PL-022-backup-service.typ"
#include "features/PL-025-multi-device-sync-service.typ"
#include "features/PL-031-file-management-service.typ"

#pagebreak(weak: true, to: "odd")
#include "known-issues.typ"
