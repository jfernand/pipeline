// Single source of truth for known rough edges in shipped features. Written
// once, here — each entry tagged with the designator(s) of the feature(s) it
// belongs to. The Known Issues page (known-issues.typ) renders the full list
// in one place; template.typ's feature() pulls in a short linked reference
// per matching designator, so a feature page never carries its own copy of
// the text to drift out of sync with this one.
#import "isss-doc.typ": ink, mono-font, rule-w, hairline

#let issues = (
  (id: "overdue-badge-align", pl: ("PL-002",), title: [Overdue badge alignment], body: [
    `OverdueBadge` (`StatusChip.kt`), as rendered next to the status chip on `AppCard`, should be
    smaller, and just tab over the top edge of the status chip it sits beside.
  ]),
  (id: "applied-line-wrap", pl: ("PL-002",), title: ['Applied' line wraps], body: [
    The Source/Added line in the detail screen (`DetailScreen.kt`, `DetailPane.kt`) is built as one
    combined string — `"Source: X · Added <date>"` — and wraps mid-line on narrow widths instead of
    laying out as a structured two-line label/value row.
  ]),
  (id: "notes-read-only", pl: ("PL-002",), title: [Notes aren't editable], body: [
    Notes on the detail screen (`detail.notes`, shown via `BodyText`) are read-only. No inline edit
    path — a change means leaving for the Add/Edit form.
  ]),
  (id: "date-applied-picker", pl: ("PL-007",), title: [Date applied has no picker], body: [
    Date applied (`AddEditScreen.kt`, `Field("Date applied", ...)`) is a plain ISO-8601 text field —
    no calendar picker. Make it properly editable from the front end instead of freehand-typed text.
  ]),
  (id: "filter-dialog", pl: ("PL-008",), title: [No advanced filter dialog], body: [
    Filtering tops out at text search plus status chips. Needs an additional button next to the
    chip row that expands into a fuller filter dialog — likely multi-field, not just status.
  ]),
  (id: "opening-animation", pl: ("PL-009",), title: [Add animation], body: [
    Screen transitions have no animation — opening an application (list → detail, including via
    deep link) should use a shared-element/shared-outline transition instead of a hard cut.
  ]),
  (id: "dev-log-caps", pl: ("PL-012",), title: [Dev log capitalizes payloads], body: [
    The event log in Dev Tools (`DevToolsContent.kt`, `EventRow`) renders payload data capitalized.
    It shouldn't transform the underlying data at all.
  ]),
  (id: "mcp-settings-row", pl: ("PL-013",), title: [MCP toggle/address should merge], body: [
    The "MCP server" toggle and "Address" `SettingsRow`s (`SettingsScreen.kt`) are two separate
    rows. Should collapse into one — with the port editable inline — and a port change should
    restart the running MCP server on the new port, not just relabel it.
  ]),
)

// Every issue label lives in this one function so the Known Issues page and
// each feature page's back-reference always agree on the name.
#let issue-label(id) = label("issue-" + id)

#let issues-for(designator) = issues.filter(it => it.pl.contains(designator))

// A small monospace tag linking back to the feature(s) an issue belongs to
// — the live "PL reference" the Known Issues page hangs off each entry.
#let pl-tag(designators) = box(stroke: rule-w + hairline, inset: (x: 6pt, y: 3pt),
  text(font: mono-font, size: 7.5pt, tracking: 0.1em, fill: ink)[
    #designators.map(d => link(label(d))[#d]).join([, ])
  ])

// The full entry, as rendered on the Known Issues page. The label comes
// after the grid, not before — a dynamically-built label only attaches to
// the element immediately preceding it; leading with it leaves it floating
// unattached, and `link()` from a feature page fails to resolve it.
#let issue-entry(it) = block(below: 12pt, breakable: false)[
  #grid(columns: (1fr, auto), column-gutter: 10pt, align: (bottom, top),
    par(leading: 4.5pt, it.body),
    pl-tag(it.pl),
  )
  #issue-label(it.id)
]
