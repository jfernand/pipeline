#import "isss-doc.typ": *
#import "template.typ": feature

#show: isss-doc.with(
  title: "Pipeline",
  subtitle: "Feature catalog — shipped and planned capabilities.",
  class: "Product Reference",
  doc-id: "ISSS-0001",
  revision: "1.0",
  date: "2026-08-19",
  status: "Current",
  applies-to: "Pipeline — Android, iOS, Desktop, Web",
  owner: "Javier",
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

#part(1, "Core Application",
  blurb: [PL-001–PL-010. What the app is for. Everything a user opens Pipeline to do, before any
    of it talks to an agent.])

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

#part(2, "Local Storage, \nData Portability & Data Sync",
  blurb: [PL-011, PL-012, PL-014. What happens to the data after it is entered — where it lives,
    what the user controls, and how it gets out again.])

#include "features/PL-011-event-sourced-local-storage.typ"
#include "features/PL-012-app-preferences-developer-mode.typ"
#include "features/PL-014-export-import-applications.typ"

#part(3, "MCP Integration",
  blurb: [PL-013, PL-015–PL-017. Same data, same actions, no separate API. An agent drives the
    pipeline the way a tap would.])

#include "features/PL-013-mcp-server-infrastructure.typ"
#include "features/PL-015-list-applications.typ"
#include "features/PL-016-list-settings.typ"
#include "features/PL-017-open-application.typ"
