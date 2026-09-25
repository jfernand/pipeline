# Documenting features in Pipeline

Pipeline tracks every feature — shipped or planned — as a standalone entry in a Typst-built
catalog under `docs/product/`. This is how that catalog works and how to add to it. If you're
adding or changing a feature, read this first.

## Why a catalog, and why Typst

A feature list that only lives in commit messages and PR descriptions can't be read end to end,
and a wiki page that isn't reviewed alongside the code it describes drifts from reality within a
month. The catalog exists so "what does this app do, and why" has one place to live, reviewed the
same way code is.

It's built in Typst, not Markdown, because the catalog is meant to be diffed like code — one
feature per file, reviewed in a PR, changed in small patches — not edited as one long
prose document.

## Designators are permanent

Every feature gets a designator, `PL-NNN`, the moment it ships or serious work starts on it.

- Numbers run in **shipping order** — `PL-001` was the first feature to ship, and each new number
  is higher than every one that came before it, whether the feature is Shipped or Planned.
- A designator is never renumbered and never reused, even after the feature it named is gone.
  Reference it in commits, PRs, and conversation the same way you'd reference a ticket number.
- To add a feature, use the next integer after the highest existing `PL-NNN` — check
  `docs/product/features/` for the current high-water mark, don't assume it matches the count of
  entries (`Planned` features consume numbers too, and numbers aren't reused if plans change).

## One file per feature

Each feature is `docs/product/features/PL-NNN-slug.typ`, built from the shared `feature()`
function in `docs/product/template.typ`:

```typst
#import "../template.typ": feature

#feature(
  designator: "PL-NNN",
  name: "Feature Name",
  status: "Shipped",   // or "Planned"
  summary: [...],       // one short paragraph: what it is
  purpose: [...],       // one short paragraph: why it exists, tied to the app's mission
  implementation: (
    "path/to/File.kt — one line on what's there",
  ),
  related: (
    ("PL-XXX", [Other Feature Name]),
  ),
)
```

Every entry has the same shape on purpose — a reference you have to relearn the structure of each
time isn't a reference. Don't add new top-level sections to individual entries; if a new field
belongs on every feature, add it to `feature()` in `template.typ` instead.

**`implementation`** is a list of real file paths, each with a short note — not prose. Leave it
empty (`()`) for a `Planned` feature with no code yet. An empty `Implementation` heading over
nothing reads as broken, not as "not started," so the template skips the heading entirely when
the list is empty — don't work around that by writing a placeholder line.

**`related`** cross-references other designators. These render as margin notes, not a bulleted
list competing with the body text. When two features are genuinely coupled both ways (an
attachment feature and the storage service that backs it, say), add the reference on both
entries — but don't force a reverse reference where the relationship only runs one direction (a
new feature citing the design system it's built on doesn't obligate the design system entry to
list every feature that uses it).

## Grouping into Parts

The catalog groups features into numbered Parts by domain — what the feature is *about*, not the
order it shipped in. See the `#part(...)` calls in `docs/product/pipeline-features.typ`. A new
feature's `#include` line goes under the Part it belongs to, in whatever position reads best
within that Part; Parts don't have to stay in strict `PL-NNN` order internally, only the
designators themselves do.

Adding a genuinely new domain (not a fit for any existing Part) is worth a new `#part(...)` —
that's how `Services` got added for background/networked work that isn't opened by hand.

## Visual design

`docs/product/isss-doc.typ` is the vendored document template (ISSS Document Design) that
`pipeline-features.typ` and every feature file build on — page furniture, headings, the cover,
Part dividers, margin cross-references, and the `Shipped`/`Planned` status stamp all live there.
Change it when the *catalog's* presentation needs to change; individual feature files should never
need their own layout code.

## Building it

```sh
cd docs/product
typst compile pipeline-features.typ                     # standard PDF
typst compile --input print=true pipeline-features.typ   # hatch fill instead of solid ink, for printing
```

The user manual (PL-026) builds the same way, from `user-manual.typ`. Its screenshots are
registered in `manual/shots.typ`: each one renders as a labelled placeholder until its PNG is saved
under `manual/screenshots/` and its ID added to `available` there.

The Data Model appendix draws its diagram with `@preview/cetz` — the one non-vendored dependency
in this build. Typst fetches and caches it locally on first compile, so that first run needs
network access; every run after that is offline again, same as the rest of the catalog.

Compile after every change and check the output — Typst errors point at the file and line, but a
silently-wrong layout (a margin note landing off the page, a heading wrapping badly) only shows up
by looking at the render.

## Workflow for a new feature

1. Pick the next `PL-NNN`.
2. Write `docs/product/features/PL-NNN-slug.typ` using `feature()`.
3. Add the `#include` line to `pipeline-features.typ`, under the right Part.
4. Cross-reference related features via `related:` (both directions, if the relationship runs
   both ways).
5. Compile both modes and check the render.
6. Stop — don't commit. Commits happen only when explicitly asked (see `AGENTS.md`).
