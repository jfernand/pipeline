// Shared layout for one entry in the Pipeline feature catalog, built on the
// vendored ISSS document design (isss-doc.typ). Every file under features/
// calls this once so entries stay structurally consistent — add a field
// here, every entry gets it.
#import "isss-doc.typ": label, ink, ink-faint, amber, amber-deep, warning, xref, rule-w

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

  // Margin blocks are anchored to their call site's position in the main
  // flow, not to each other — two calls fired back-to-back (as `warning`
  // and `xref` would be, right after the heading) land at nearly the same
  // y-coordinate and their multi-line text overlaps. Firing `warning` here
  // and `xref` only after Purpose/Implementation gives them the full height
  // of the summary and purpose text as clearance.
  if status == "Planned" {
    warning[Planned — not yet implemented. Tracked here so the gap between
      what the app shows and what actually works stays visible.]
  }

  summary
  v(4pt)

  heading(level: 4, numbering: none)[Purpose]
  purpose

  heading(level: 4, numbering: none)[Implementation]
  list(..implementation.map(item => raw(item)))

  if related.len() > 0 {
    let to = related.map(r => r.at(0)).join(", ")
    let body = related.map(r => r.at(1)).join([; ])
    xref(to, body)
  }
}
