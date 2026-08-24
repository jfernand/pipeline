// Shared layout for one entry in the Pipeline feature catalog, built on the
// vendored ISSS document design (isss-doc.typ). Every file under features/
// calls this once so entries stay structurally consistent — add a field
// here, every entry gets it.
#import "isss-doc.typ": label, ink, ink-faint, amber, amber-deep, xref, rule-w
#import "issues.typ": issue-label, issue-number, issues-for

// A bordered stamp, same construction as the cover's own classification
// stamp — makes Shipped/Planned scannable at a glance across the whole
// document, not just readable one entry at a time.
#let status-stamp(status) = {
  let accent = if status == "Planned" { amber } else { ink }
  let text-color = if status == "Planned" { amber-deep } else { ink-faint }
  box(stroke: rule-w + accent, inset: (x: 6pt, y: 3pt), label(status, color: text-color))
}

// A small filled badge — distinct from status-stamp's outlined pill — for which release a
// feature belongs to. Filled rather than outlined specifically so it doesn't get mistaken for
// another status pill next to status-stamp.
#let release-badge(release) = box(fill: amber, inset: (x: 6pt, y: 3pt), label(release, color: ink))

#let feature(
  designator: "",
  name: "",
  status: "Shipped",
  release: none, // e.g. "MVP" — which release this feature ships in, if assigned to one yet
  summary: [],
  purpose: [],
  description: [],
  implementation: (),
  related: (), // array of (designator, description-content) pairs
) = {
  // The designator gets its own line — sharing a line with a long name was
  // wrapping mid-title, breaking the designator away from its own name.
  //
  // The label right after it is what makes `link(label("PL-002"))` jump
  // here from the Known Issues page. Two things make this work: it has to
  // sit inside a markup content block (`[...]`) — a dynamically-built label
  // only attaches to the preceding element when joined the way markup joins
  // adjacent content, not via a bare statement in a `{...}` code block
  // (Typst rejects that join outright). And it has to be `std.label`, not
  // the bare `label(...)` call: this file imports `label` from isss-doc.typ
  // as the mono-caps text styler used just below, which shadows the
  // built-in label constructor — `std` reaches past that back to the real
  // one.
  [
    #heading(level: 1, numbering: none)[#designator #linebreak() #name]
    #std.label(designator)
    // Feeds the Feature Index appendix — designator, name and status read
    // straight off this call rather than duplicated into a second table
    // somewhere, so the index can't drift out of sync with the entry
    // itself. Page number comes from this element's own location once the
    // appendix queries for it.
    #metadata((designator: designator, name: name, status: status, release: release)) <feature-meta>
  ]
  block(below: 12pt, {
    status-stamp(status)
    if release != none { h(6pt); release-badge(release) }
  })

  summary
  v(4pt)

  heading(level: 4, numbering: none)[Purpose]
  purpose

  // Nothing to point to yet is itself information — every Shipped entry, and
  // every Planned one with real scaffolding, has at least one file here.
  // Showing an empty "Implementation" heading over nothing would read as
  // broken, not as "not started," so skip the section entirely instead.
  if implementation.len() > 0 {
    heading(level: 4, numbering: none)[Implementation]
    list(..implementation.map(item => raw(item)))
  }

  // Known rough edges in what's already shipped — pulled from the one
  // registry in issues.typ by designator, not passed in here, so the text
  // lives in exactly one place: the Known Issues page. This is just a
  // linked pointer to it.
  let my-issues = issues-for(designator)
  if my-issues.len() > 0 {
    heading(level: 4, numbering: none)[Known Issues]
    list(..my-issues.map(it => link(issue-label(it.id))[#issue-number(it) — #it.title]))
  }

  if related.len() > 0 {
    // `xref` (via `_margin-place`) anchors its `place()` call to whatever
    // block last settled the flow. Immediately after a paragraph — which is
    // what precedes this when Implementation was skipped — there is no such
    // anchor yet, and the margin block resolves to the wrong position
    // (overlapping body text, or off the page entirely) instead of the
    // outer column. A block-level `v(0pt)` is a real anchor and costs no
    // visible space, so it's cheap enough to emit unconditionally rather
    // than only when Implementation was actually skipped.
    v(0pt)
    let to = related.map(r => r.at(0)).join(", ")
    let body = related.map(r => r.at(1)).join([; ])
    xref(to, body)
  }

  if description != none {
      heading(level: 4, numbering: none)[Description]
      description
  }
}
