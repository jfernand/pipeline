// =============================================================================
// isss-doc.typ — Industrial Strength Software Services
// Document template for internal product documents and user manuals.
//
//   #import "isss-doc.typ": *
//   #show: isss-doc.with(
//     title: "FORGE Dashboard",
//     subtitle: "Operations and project tracking for client engagements.",
//     class: "User Manual",
//     doc-id: "ISSS-UM-0041",
//     revision: "4.2",
//     date: "2026-08-19",
//     classification: "Internal — do not distribute",   // "" prints nothing
//   )
//
// Tested against Typst 0.12.
//
// Vendored from the "ISSS Document Design" Claude Design project
// (37738d26-6486-4062-935f-f6a2dffb6d67) verbatim, with one fix: the source's
// `ink-second` was defined as `ink-second   // --color-steel-700` (a
// self-reference that fails to compile — the name doesn't exist yet at that
// point). Filled in as `#333333`, the next step darker than `ink-muted`
// (#4A4A4A, steel-600) on the documented steel-200/500/600 progression.
// =============================================================================

// ---------------------------------------------------------------- ink --------
// All values below are tokens from the ISSS design system
// (colors_and_type.css). Do not introduce colours that are not in that file.
#let paper-bg    = rgb("#F0EBE1")   // --color-paper
#let ink         = rgb("#0E0E0E")   // --color-black   · headings, rules
#let ink-body    = rgb("#1A1A1A")   // --color-ink     · body text, 14.7:1
#let ink-second  = rgb("#333333")   // --color-steel-700 (patched, see header note)
#let ink-muted   = rgb("#4A4A4A")   // --color-steel-600
#let ink-faint   = rgb("#666666")   // --color-steel-500 · furniture labels
#let hairline    = rgb("#CCCCCC")   // --color-steel-200
#let panel       = rgb("#E8E3D8")   // --bg-overlay (light)
#let panel-sunk  = rgb("#DEDAD0")   // --bg-sunken (light) · zebra, screenshots
#let amber       = rgb("#F5B800")   // --color-amber-400 · rules and fills
#let amber-deep  = rgb("#C98A00")   // --color-amber-600 · 2.5:1 on paper
#let danger      = rgb("#CC2200")   // --color-red · 4.7:1 on paper

// Neither amber clears AA on paper (2.5:1 at best), so on paper pages amber is
// rules, panel edges and fills — never text. Structural numbers and marks are
// `ink`. On the black cover and dividers, amber on #0E0E0E is 10.8:1 and is
// used for text freely.

// --------------------------------------------------------------- type --------
// `typst fonts --variants` (against the real Barlow Condensed TTFs in
// fonts/, pulled from Google Fonts to match ISSS Document Design.dc.html's
// own <link>) reports their usable family as plain "Barlow" — Style:
// Normal, Weight: 600/700/800, Stretch: 75% (the condensed width is
// stretch metadata, not part of the name Typst matches on).
#let display-font = "Barlow"
#let body-font    = "Space Grotesk"
#let mono-font    = "IBM Plex Mono"

#let rule-w = 0.5pt

// A tracked mono caps label — the house UI stamp.
#let label(body, color: ink-faint, size: 7.5pt) = text(
  font: mono-font, size: size, weight: 500,
  tracking: 0.14em, fill: color,
)[#upper(body)]

// ============================================================ geometry =======
// One text measure plus a wide outer margin column. Mirrors on the spread.
#let geometry = (
  a4: (
    size: "a4",
    inner: 54pt, outer: 58pt, top: 42pt, bottom: 42pt,
    measure: 348pt, gutter: 18pt, margin-col: 117pt,
  ),
  letter: (
    size: "us-letter",
    inner: 54pt, outer: 62pt, top: 42pt, bottom: 42pt,
    measure: 348pt, gutter: 18pt, margin-col: 117pt,
  ),
)

// State shared with the sidenote machinery and the page furniture.
#let _geo = state("isss-geo", geometry.a4)
#let _meta = state("isss-meta", (:))
#let _sidenote-counter = counter("isss-sidenote")

// ============================================================ margin =========
// Everything that lives in the outer column goes through _margin-place, so the
// verso/recto mirroring is decided in exactly one place.
#let _margin-place(body) = context {
  let g = _geo.get()
  let recto = calc.odd(here().page())
  // Verso is placed `top + right` (block right-aligned to the anchor), not
  // `top + left` like recto — so its dx must land the block's *right* edge
  // at the gutter boundary (-gutter), not its left edge at the far outer
  // margin (-(margin-col + gutter), what a left-aligned block would need).
  // Using the left-aligned distance here pushed the whole block a further
  // margin-col past the page's left edge on every verso page.
  let dx = if recto { g.measure + g.gutter } else { -g.gutter }
  box(width: 0pt, height: 0pt, place(
    top + if recto { left } else { right },
    dx: dx, dy: 0.15em,
    block(width: g.margin-col, body),
  ))
}

/// A sidenote. ISSS documents have no footnotes; this is the replacement.
/// The marker is a superscript number in the text, the note sits beside it.
#let sidenote(body) = {
  _sidenote-counter.step()
  context {
    let n = _sidenote-counter.get().first()
    super(text(font: mono-font, size: 7pt, fill: ink)[#n])
    _margin-place[
      #grid(columns: (10pt, 1fr), column-gutter: 5pt,
        text(font: mono-font, size: 8pt, fill: ink)[#n],
        par(leading: 4.5pt, text(font: body-font, size: 8.5pt, fill: ink-muted, body)),
      )
    ]
  }
}

/// A cross-reference in the margin. `to` is the pointer, body says why.
#let xref(to, body) = _margin-place[
  #block(above: 0pt, stroke: (top: rule-w + hairline), inset: (top: 5pt))[
    #text(font: mono-font, size: 8pt, fill: ink)[→ #to]
    #linebreak()
    #par(leading: 4.5pt, text(font: body-font, size: 8.5pt, fill: ink-muted, body))
  ]
]

/// Marks a passage as changed since the previous revision.
#let revised(rev, body: [Changed in this revision.]) = _margin-place[
  #grid(columns: (auto, 1fr), column-gutter: 5pt,
    box(stroke: rule-w + amber, inset: (x: 3pt, y: 1pt),
      text(font: mono-font, size: 6.5pt, tracking: 0.12em, fill: ink)[#rev]),
    par(leading: 4pt, text(font: body-font, size: 8pt, fill: ink-faint, body)),
  )
]

// ======================================================== admonitions ========
// note and warning live in the margin. danger is inline at full measure —
// if it can destroy something, it does not get to hide in the gutter.

#let note(body) = _margin-place[
  #block(above: 0pt, stroke: (top: rule-w + amber), inset: (top: 5pt))[
    #label("Note", color: ink, size: 7pt)
    #linebreak()
    #par(leading: 4.5pt, text(font: body-font, size: 8.5pt, fill: ink-second, body))
  ]
]

#let warning(body) = _margin-place[
  #block(fill: panel, stroke: (left: 1.5pt + amber), inset: (x: 8pt, y: 7pt))[
    #label("Warning", color: ink, size: 7pt)
    #linebreak()
    #par(leading: 4.5pt, text(font: body-font, size: 8.5pt, fill: ink-body, body))
  ]
]

#let danger-block(body) = block(
  width: 100%, above: 14pt, below: 14pt,
  fill: panel,
  stroke: (paint: danger, thickness: rule-w, left: 1.5pt + danger),
  inset: (x: 11pt, y: 9pt),
)[
  #label("Danger", color: danger, size: 7pt)
  #linebreak()
  #par(leading: 4.5pt, text(font: body-font, size: 9pt, fill: ink-body, body))
]

// ============================================================== code =========
// Light panel for source. Inverted panel for terminal transcripts.

#let listing(body, caption: none, number: none) = {
  block(
    width: 100%, above: 16pt, below: if caption == none { 16pt } else { 5pt },
    fill: panel,
    stroke: (paint: hairline, thickness: rule-w, left: 1.5pt + amber),
    inset: (x: 12pt, y: 10pt),
    text(font: mono-font, size: 8.5pt, fill: ink-body, body),
  )
  if caption != none {
    block(below: 16pt, label([Listing #number — #caption]))
  }
}

#let transcript(body, caption: none, number: none) = {
  block(
    width: 100%, above: 16pt, below: if caption == none { 16pt } else { 5pt },
    fill: ink, inset: (x: 12pt, y: 10pt), radius: 0pt,
    text(font: mono-font, size: 8.5pt, fill: paper-bg, body),
  )
  if caption != none {
    block(below: 16pt, label([Transcript #number — #caption]))
  }
}

/// The amber prompt glyph for use inside `transcript`.
#let prompt = text(fill: amber)[\$]

// ============================================================= tables ========
// Horizontal rules only. Zebra tint on even rows. Mono caps header.
#let data-table(columns: (), header: (), ..rows) = {
  set text(font: body-font, size: 9pt, fill: ink-body)
  table(
    columns: columns,
    stroke: none,
    inset: (x: 0pt, y: 5pt),
    fill: (_, y) => if y == 0 { none } else if calc.even(y) { panel } else { none },
    table.header(..header.map(h => label(h))),
    table.hline(y: 1, stroke: rule-w + ink),
    ..rows.pos(),
  )
}

#let table-caption(number, body) = block(above: 5pt, below: 16pt,
  label([Table #number — #body]))

// ============================================================ figures =======
#let figure-caption(number, body) = block(above: 5pt, below: 16pt)[
  #grid(columns: (auto, 1fr), column-gutter: 10pt,
    label([Fig #number], color: ink),
    par(leading: 4.5pt, text(font: body-font, size: 8.5pt, fill: ink-muted, body)),
  )
]

/// A figure that may run wide: measure only, or measure + margin column.
#let plate(body, caption: none, number: none, wide: false) = context {
  let g = _geo.get()
  let w = if wide { g.measure + g.gutter + g.margin-col } else { g.measure }
  block(width: w, above: 16pt, below: 5pt,
    box(width: 100%, stroke: rule-w + hairline, body))
  if caption != none { figure-caption(number, caption) }
}

/// Screenshot placeholder — real screenshots go in desaturated.
#let screenshot(height: 190pt, note: "Screenshot") = box(
  width: 100%, height: height, fill: panel-sunk,
  align(center + horizon, label(note)),
)

// A page that faces the front (the cover, a part divider) is followed by a
// deliberately blank verso — no body text — rather than letting the next
// content print on its back. It still carries the normal running head and
// folio: a blank page is still a page, not a hole in the furniture.
// Unconditional (not `weak: true`): the page right after `part()`'s own
// `page(...)` call is already pending-empty, and a weak break would just
// coalesce into it instead of leaving it blank.
#let _blank-back() = {
  page[]
}

// ====================================================== part dividers =======
/// A full-bleed black part divider. Only place besides the cover where
/// Barlow runs large.
#let part(number, name, blurb: none) = {
  pagebreak(weak: true, to: "odd")
  page(
    fill: ink, margin: (left: 54pt, right: 58pt, top: 42pt, bottom: 42pt),
    header: none, footer: none,
  )[
    #v(300pt)
    #label([Part #number], color: amber, size: 8pt)
    #v(10pt)
    #text(font: display-font, size: 57pt, weight: 800, fill: paper-bg,
      tracking: 0.005em)[#upper(name)]
    #v(16pt)
    #box(width: 90pt, height: rule-w, fill: amber)
    #if blurb != none [
      #v(16pt)
      #block(width: 300pt, par(leading: 6pt, text(font: body-font, size: 12pt,
        fill: rgb("#AAAAAA"), blurb)))
    ]
  ]
  _blank-back()
}

// ============================================================== cover =======
#let _cover(m) = {
  let g = m.geo
  page(
    fill: ink, margin: (left: g.inner, right: g.outer, top: g.top, bottom: g.bottom),
    header: none, footer: none,
  )[
    // Hairline grid keyed to the text block: the cover shows the page geometry.
    // Stops level with the amber rule below (metadata-block height + its
    // 74pt bottom offset) rather than running the full page height and
    // cutting straight through it.
    #place(top + left, dx: -g.inner, dy: -g.top, box(width: 100%, height: 100% - 176pt)[
      #place(top + left, dx: g.inner, line(angle: 90deg, length: 100%,
        stroke: rule-w + rgb("#3A3A3A")))
      #place(top + left, dx: g.inner + g.measure, line(angle: 90deg, length: 100%,
        stroke: rule-w + rgb("#3A3A3A")))
    ])

    #grid(columns: (1fr, auto), align: horizon,
      grid(columns: (auto, auto), column-gutter: 10pt, align: horizon,
        box(width: 16pt, height: 16pt, stroke: 1pt + amber,
          align(center + horizon, box(width: 6pt, height: 6pt, fill: amber))),
        text(font: mono-font, size: 7.5pt, tracking: 0.2em, fill: paper-bg)[
          INDUSTRIAL STRENGTH SOFTWARE SERVICES],
      ),
      if m.classification != "" {
        box(stroke: rule-w + amber, inset: (x: 6pt, y: 3pt),
          label(m.classification, color: amber, size: 7pt))
      },
    )

    #v(255pt)
    #label(m.class, color: amber, size: 8pt)
    #v(11pt)
    #block(stroke: (left: 1.5pt + amber), inset: (left: 18pt))[
      #text(font: display-font, size: 66pt, weight: 800, fill: paper-bg,
        tracking: 0.005em)[#upper(m.title)]
      #if m.subtitle != none [
        #v(13pt)
        #block(width: 250pt, par(leading: 6pt, text(font: body-font, size: 14pt,
          fill: rgb("#AAAAAA"), m.subtitle)))
      ]
    ]

    #place(bottom + left, dy: -74pt, box(width: 100%)[
      #line(length: 100%, stroke: rule-w + amber)
      #v(18pt)
      #let f(k, v, c: paper-bg) = [
        #label(k, size: 7pt)
        #linebreak()
        #text(font: mono-font, size: 8.5pt, fill: c)[#v]
      ]
      #grid(columns: (1fr, 1fr), column-gutter: 40pt,
        stack(spacing: 12pt,
          f("Revision", m.revision),
          f("Date", m.date),
          f("Status", m.status, c: amber)),
        stack(spacing: 12pt,
          f("Document ID", m.doc-id),
          f("Applies to", m.applies-to),
          f("Owner", m.owner)),
      )
    ])

    #place(bottom + left, box(width: 100%)[
      #line(length: 100%, stroke: rule-w + rgb("#2E2E2E"))
      #v(7pt)
      #grid(columns: (1fr, auto),
        label(m.supersedes), label(m.page-count))
    ])
  ]
}

// =========================================================== contents =======
#let contents() = {
  // A level-1 heading's own body may carry a hard linebreak (feature() puts
  // the PL-NNN designator on its own line so a long name can't wrap into
  // it) — reused verbatim by the default outline entry, that would print
  // the TOC line broken in two as well. Flatten just the level-1 entries
  // back to one line here, using the same prefix/fill/page-link parts the
  // stock renderer builds from, so nothing else about the TOC changes.
  show outline.entry.where(level: 1): it => {
    let body = it.element.body
    // Plain-text headings (no linebreak) are a bare `text`, not a
    // `sequence` — only sequences expose `.children` to walk.
    let flat = if body.has("children") {
      body.children.map(c => if c == linebreak() { [ ] } else { c }).sum()
    } else {
      body
    }
    link(it.element.location(), it.indented(none, flat + box(width: 1fr, it.fill) + it.page()))
  }
  page[
    #text(font: display-font, size: 33pt, weight: 800, tracking: 0.01em)[CONTENTS]
    #v(-2pt)
    #line(length: 100%, stroke: rule-w + ink)
    #v(16pt)
    #outline(title: none, indent: n => n * 12pt, depth: 3)
  ]
}

// ========================================================= the template =====
#let isss-doc(
  title: "Untitled",
  subtitle: none,
  class: "Internal Document",
  doc-id: "ISSS-XX-0000",
  revision: "0.1",
  date: datetime.today().display(),
  status: "Draft",
  applies-to: "—",
  owner: "—",
  supersedes: "—",
  page-count: "",
  classification: "",          // "" → no stamp anywhere, rules collapse
  size: "a4",                  // "a4" | "letter"
  cover: true,
  toc: true,
  body,
) = {
  let g = geometry.at(size)
  let m = (
    title: title, subtitle: subtitle, class: class, doc-id: doc-id,
    revision: revision, date: date, status: status, applies-to: applies-to,
    owner: owner, supersedes: supersedes, page-count: page-count,
    classification: classification, geo: g,
  )

  _geo.update(g)
  _meta.update(m)

  set document(title: title, author: "Industrial Strength Software Services")

  // ---- body pages: paper, two-sided, wide outer margin -------------------
  set page(
    paper: g.size,
    fill: paper-bg,
    margin: (inside: g.inner, outside: g.outer + g.gutter + g.margin-col,
             top: g.top + 30pt, bottom: g.bottom + 26pt),
    binding: left,
    header: context {
      // `.before(here())` excludes a heading sitting at the very top of
      // *this* page — `here()` resolves to a position the header itself
      // is considered to precede, so a chapter's own opening H1 (forced to
      // the top of a fresh page by its show-rule's `pagebreak`) never
      // counted as "seen" on its own page, and the header lagged one
      // chapter behind for the whole rest of the document. Comparing page
      // numbers instead — "on this page or earlier" — fixes that; it's the
      // same page-number approach the `sub` filter below already needs.
      let pg = here().page()
      let h = query(heading).filter(x => x.location().page() <= pg)
      let sec = if h.len() > 0 {
        let top = h.filter(x => x.level == 1)
        // An H2 only counts as the current "sub" if it falls on or after the
        // current chapter's own H1 — otherwise, on a chapter with no H2 of
        // its own, `sub` would keep echoing the last H2 from a *previous*
        // chapter.
        let last-top-page = if top.len() > 0 { top.last().location().page() } else { 0 }
        let sub = h.filter(x => x.level == 2 and x.location().page() >= last-top-page)
        (
          if sub.len() > 0 { upper(sub.last().body) } else { "" },
          if top.len() > 0 { upper(top.last().body) } else { upper(title) },
        )
      } else { ("", upper(title)) }
      let recto = calc.odd(here().page())
      let (near, far) = if recto { (sec.at(0), sec.at(1)) } else { (sec.at(1), sec.at(0)) }
      block(width: 100%, below: 0pt,
        stroke: (bottom: rule-w + hairline), inset: (bottom: 6pt))[
        #grid(columns: (1fr, auto), label(near), label(far))
      ]
    },
    footer: context {
      let recto = calc.odd(here().page())
      let stamp = if classification != "" { classification } else { "" }
      let cols = (label(stamp), label[#doc-id · Rev #revision],
                  label(counter(page).display()))
      block(width: 100%, above: 0pt,
        stroke: (top: rule-w + hairline), inset: (top: 6pt))[
        #grid(columns: (1fr, auto, 1fr),
          align: (left, center, right),
          ..(if recto { cols } else { cols.rev() }))
      ]
    },
  )

  // ---- text --------------------------------------------------------------
  set text(font: body-font, size: 10pt, fill: ink-body, lang: "en")
  // `show par: set block(spacing:)` (the source's original second line here)
  // is a no-op on this Typst version — paragraphs aren't blocks anymore, and
  // `spacing: 9pt` is already covered by `set par` above.
  set par(leading: 6pt, spacing: 9pt, justify: false)

  set list(marker: text(fill: ink)[—], indent: 0pt, body-indent: 9pt)
  set enum(numbering: n => text(font: mono-font, size: 9pt, fill: ink)[#n.],
           indent: 0pt, body-indent: 9pt)

  show raw: set text(font: mono-font, size: 9pt)
  show emph: set text(style: "italic")
  show link: set text(fill: ink)

  // ---- headings ----------------------------------------------------------
  set heading(numbering: "1.1.1")

  show heading.where(level: 1): it => {
    pagebreak(weak: true)
    block(below: 16pt, above: 0pt,
      stroke: (bottom: rule-w + ink), inset: (bottom: 7pt), width: 100%)[
      #text(font: display-font, size: 26pt, weight: 800, fill: ink, tracking: 0.01em)[
        #if it.numbering != none [
          #text(fill: ink)[#counter(heading).display(it.numbering)]
          #h(13pt)
        ]
        #upper(it.body)
      ]
    ]
  }

  show heading.where(level: 2): it => block(above: 20pt, below: 11pt,
    stroke: (bottom: rule-w + ink), inset: (bottom: 6pt), width: 100%)[
    #text(font: display-font, size: 14pt, weight: 800, fill: ink, tracking: 0.03em)[
      #if it.numbering != none [
        #text(fill: ink)[#counter(heading).display(it.numbering)]
        #h(11pt)
      ]
      #upper(it.body)
    ]
  ]

  // Barlow gets cramped below ~13pt, so H3 and down are Space Grotesk.
  show heading.where(level: 3): it => block(above: 16pt, below: 6pt,
    text(font: body-font, size: 11pt, weight: 700, fill: ink)[
      #if it.numbering != none [
        #counter(heading).display(it.numbering)#h(7pt)
      ]
      #it.body
    ])

  show heading.where(level: 4): it => block(above: 13pt, below: 5pt,
    text(font: body-font, size: 10pt, weight: 700, fill: ink)[#it.body])

  // ---- front matter ------------------------------------------------------
  // Every piece of front matter that faces the reader head-on (cover, TOC)
  // gets a blank back, same as a part divider — nothing prints on the flip
  // side of a title page.
  if cover { _cover(m); _blank-back() }
  if toc { contents(); _blank-back() }

  // Body numbering resets to 1 here. Recto/verso placement (every margin
  // note, every header) keys off the *physical* page count, which never
  // resets — the blank-backs above keep the two in lockstep only because
  // today's cover and TOC are each exactly one page. If either ever grows,
  // recheck this by hand: an extra unpaired page here lands displayed
  // page "1" on a physically even (left/verso) page instead of the odd
  // (right/recto) page a reader expects "page 1" to be on.
  counter(page).update(1)
  body
}

/// Appendices: letters instead of numbers, otherwise identical.
#let appendices(body) = {
  counter(heading).update(0)
  set heading(numbering: "A.1.1")
  body
}
