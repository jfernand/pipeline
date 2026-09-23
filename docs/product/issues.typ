// Single source of truth for known rough edges in shipped features. Written
// once, here — each entry tagged with the designator(s) of the feature(s) it
// belongs to. The Known Issues page (known-issues.typ) renders the full list
// in one place; template.typ's feature() pulls in a short linked reference
// per matching designator, so a feature page never carries its own copy of
// the text to drift out of sync with this one.
#import "isss-doc.typ": ink, mono-font, rule-w, hairline

// `num` is a stable per-primary-feature sequence, not a list position —
// assigned once, in the order an issue was filed, and never renumbered or
// reused, same as a PL-NNN feature designator itself. Filing a new issue
// picks the next number after the highest already used for that issue's
// `pl.at(0)`; moving an entry from `issues` to `fixed-issues` never changes
// its number.
#let issues = (
  (id: "deb-taskswitch-icon", pl: ("PL-039",), num: 1, title: [Debian package shows no icon when switching tasks], body: [
    The `.deb`-packaged desktop build (`packageReleaseDeb`, `desktopApp/build.gradle.kts`'s
    `linux { iconFile.set(...) }`) shows a generic icon instead of Pipeline's mark in the task
    switcher (Alt+Tab / window list), even though `Window(icon = painterResource("icon.png"))`
    (`desktopApp/src/main/kotlin/org/cr/pipeline/main.kt`) sets a titlebar icon correctly. Likely
    the generated `.desktop` entry's `Icon=`/`StartupWMClass` isn't resolving against an installed
    icon theme entry the window manager can find — worth checking what jpackage actually writes
    for the Linux bundle.
  ]),
  (id: "no-delete-application", pl: ("PL-002",), num: 4, title: [No way to delete a job application], body: [
    The Delete icon on the phone detail screen (`DetailScreen.kt`'s `PlTopBar` `rightActions`) is
    wired to an empty `{}` lambda — it does nothing. Tablet's `DetailPane.kt` doesn't even have a
    delete affordance in its header actions. There's no path, on either layout, to remove an
    application once it's been added.
  ]),
)

// Resolved entries, moved here verbatim from `issues` rather than edited or summarized — this is
// the record of what the issue actually said, not a changelog blurb about it. `known-issues.typ`
// renders these in their own "Fixed" section, separate from the still-open list above.
#let fixed-issues = (
  (id: "notes-read-only", pl: ("PL-002",), num: 3, title: [Notes aren't editable], body: [
    Notes on the detail screen (`detail.notes`, shown via `BodyText`) are read-only. No inline edit
    path — a change means leaving for the Add/Edit form.
  ]),
  (id: "escape-to-cancel", pl: ("PL-032", "PL-002"), num: 1, title: [No Escape-to-cancel], body: [
    Editable affordances have no cancel path. `NotesSection`'s `BasicTextField`
    (`DetailSections.kt`) only ever commits — on blur or on Return — there's no key that discards
    the draft and restores the original text. Escape should do that: leave the data unchanged and
    drop back to the static display, the way it does in most inline editors.
  ]),
  (id: "applied-line-wrap", pl: ("PL-002",), num: 2, title: ['Applied' line wraps], body: [
    The Source/Added line in the detail screen (`DetailScreen.kt`, `DetailPane.kt`) is built as one
    combined string — `"Source: X · Added <date>"` — and wraps mid-line on narrow widths instead of
    laying out as a structured two-line label/value row.
  ]),
  (id: "dev-log-caps", pl: ("PL-012",), num: 1, title: [Dev log capitalizes payloads], body: [
    The event log in Dev Tools (`DevToolsContent.kt`, `EventRow`) renders payload data capitalized.
    It shouldn't transform the underlying data at all.
  ]),
  (id: "mcp-settings-row", pl: ("PL-013",), num: 1, title: [MCP toggle/address should merge], body: [
    The "MCP server" toggle and "Address" `SettingsRow`s (`SettingsScreen.kt`) are two separate
    rows. Should collapse into one — with the port editable inline — and a port change should
    restart the running MCP server on the new port, not just relabel it.
  ]),
  (id: "dev-log-order", pl: ("PL-012",), num: 2, title: [Dev log entries should be reverse order], body: [
    The event log in Dev Tools (`DevToolsContent.kt`, `EventRow`) renders `chain` in append order —
    oldest first, newest at the bottom. It should be reverse order, last on top, so the newest
    entry is the one you see without scrolling.
  ]),
  (id: "date-applied-picker", pl: ("PL-007",), num: 1, title: [Date applied has no picker], body: [
    Date applied (`AddEditScreen.kt`, `Field("Date applied", ...)`) is a plain ISO-8601 text field —
    no calendar picker. Make it properly editable from the front end instead of freehand-typed text.
  ]),
  (id: "phone-sync-devtools-nav", pl: ("PL-009", "PL-003", "PL-012"), num: 1, title: [Sync and Dev Tools unreachable on phone], body: [
    `NavRail` (tablet-only) is the only way to reach the Sync tab (`TabletSyncContent`) and Dev
    Tools (`DevToolsContent`) — `PipelinePhoneApp`'s `NavHost` never wired `SyncRoute` or
    `DevToolsRoute` at all, so on phone-width screens (portrait, or landscape narrower than the
    600dp tablet-layout breakpoint) there's no way to reach either, even with developer mode on.
  ]),
  (id: "filter-chips-overflow", pl: ("PL-008",), num: 1, title: [Filter chips run off screen on portrait phone], body: [
    `StatusFilterChips` (`ListScreen.kt`) sits in a `Row(Modifier.horizontalScroll(...))` on
    phone — with 8 chips (All + one per `AppStatus`), that runs off the edge of a portrait phone
    screen instead of wrapping. Tablet's `ListPane.kt` already wraps the same chips in a
    `FlowRow`; phone should too.
  ]),
  (id: "about-version-hardcoded", pl: ("PL-012",), num: 3, title: [About box shows a hardcoded version], body: [
    Settings' "About" row (`SettingsScreen.kt`) reads `"Pipeline 1.4.0"` / `"Build 2026.06.24"` —
    fixed strings that never move, unlike `NavRail`'s footer, which already reads the real
    `BuildInfo.GIT_DESCRIBE`. The two should agree.
  ]),
  (id: "overdue-badge-align", pl: ("PL-002",), num: 1, title: [Overdue badge alignment], body: [
    `OverdueBadge` (`StatusChip.kt`), as rendered next to the status chip on `AppCard`, should be
    smaller, and just tab over the top edge of the status chip it sits beside.
  ]),
)

// Every issue label lives in this one function so the Known Issues page and
// each feature page's back-reference always agree on the name.
#let issue-label(id) = label("issue-" + id)

#let issues-for(designator) = issues.filter(it => it.pl.contains(designator))

// Zero-padded to 3 digits — PL-002-001, not PL-002-1 — so the column of
// numbers on the Known Issues page lines up instead of ragging.
#let _pad3(n) = {
  let s = str(n)
  "0" * calc.max(0, 3 - s.len()) + s
}

// The issue's own designator: its primary feature (pl.at(0), "the main
// feature associated with the issue") plus its stable per-feature sequence
// number. Stable and permanent the same way a PL-NNN designator is — see
// the comment on `issues` above.
#let issue-number(it) = it.pl.at(0) + "-" + _pad3(it.num)

// The small monospace tag on each Known Issues entry: the issue's own
// PL-XXXX-YYY first (linking to its primary feature), then any other
// related features it also touches, plain, same as before.
#let pl-tag(it) = box(stroke: rule-w + hairline, inset: (x: 6pt, y: 3pt),
  text(font: mono-font, size: 7.5pt, tracking: 0.1em, fill: ink)[
    #{
      let primary = link(label(it.pl.at(0)))[#issue-number(it)]
      let rest = it.pl.slice(1).map(d => link(label(d))[#d])
      ((primary,) + rest).join([, ])
    }
  ])

// The full entry, as rendered on the Known Issues page. The label comes
// after the grid, not before — a dynamically-built label only attaches to
// the element immediately preceding it; leading with it leaves it floating
// unattached, and `link()` from a feature page fails to resolve it.
#let issue-entry(it) = block(below: 12pt, breakable: false)[
  #grid(columns: (1fr, auto), column-gutter: 10pt, align: (bottom, top),
    par(leading: 4.5pt, it.body),
    pl-tag(it),
  )
  #issue-label(it.id)
]
