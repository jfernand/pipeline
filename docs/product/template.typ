// Shared layout for one entry in the Pipeline feature catalog, built on the
// vendored ISSS document design (isss-doc.typ). Every file under features/
// calls this once so entries stay structurally consistent — add a field
// here, every entry gets it.
#import "isss-doc.typ": label, ink, ink-faint, amber, amber-deep, xref, rule-w

// A bordered stamp, same construction as the cover's own classification
// stamp — makes Shipped/Planned scannable at a glance across the whole
// document, not just readable one entry at a time.
#let status-stamp(status) = {
  let accent = if status == "Planned" { amber } else { ink }
  let text-color = if status == "Planned" { amber-deep } else { ink-faint }
  box(stroke: rule-w + accent, inset: (x: 6pt, y: 3pt), label(status, color: text-color))
}

#let feature(
  designator: "",
  name: "",
  status: "Shipped",
  summary: [],
  purpose: [],
  implementation: (),
  related: (), // array of (designator, description-content) pairs
) = {
  // The designator gets its own line — sharing a line with a long name was
  // wrapping mid-title, breaking the designator away from its own name.
  heading(level: 1, numbering: none)[#designator #linebreak() #name]
  block(below: 12pt, status-stamp(status))

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
}
