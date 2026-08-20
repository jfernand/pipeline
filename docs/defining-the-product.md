# Defining and documenting a product

This is the level above [docs/writing-features.md](writing-features.md) — not how to write one
feature entry well, but how to define and document the product those entries belong to. It's
written as guidance for a PM, using Pipeline's own product doc
(`docs/product/pipeline-features.typ`) as the working example throughout.

## The mission statement's job is to exclude

A mission statement that only says what the product does invites every plausible feature in,
because almost anything can be argued to help. Its real job is to also say what the product
*doesn't* do, so that boundary exists somewhere other than a PM's memory.

Pipeline's is one paragraph:

> Pipeline tracks job applications. Company, role, status, dates, contacts, follow-ups. Nothing
> else, and nothing less.

"Nothing else" is doing the work. It's why a feature like a general task manager, or a resume
builder with its own editor, doesn't belong here even though a "job search app" could plausibly
grow either — they're not tracking an application, they're a different product. If your mission
statement can't be used to say no to a real, tempting feature request, it isn't specific enough
yet to say yes to anything either.

Write it short enough to hold in your head, because you're going to need it every time someone —
including you — proposes something new. See [docs/writing-features.md](writing-features.md) for
how every individual feature's `purpose` should trace back to this same sentence.

## Defending the boundary is the ongoing job

Writing the mission once is the easy part. The actual work is saying no to things that are
individually reasonable but don't belong — a good idea for a *different* product isn't a good idea
for this one. When you do decide something belongs that seems to stretch the boundary (Pipeline's
MCP server is arguably "more than tracking applications"), write down why explicitly rather than
letting it slide in unexamined:

> It also runs an MCP server, and that is not a bolt-on. An agent reads and writes the same data
> the UI does — not a copy, not an export. The same pipeline, through a different door.

That paragraph exists specifically because MCP integration could otherwise look like scope creep.
Naming the reason turns a judgment call into something the next reader can evaluate, instead of
an assumption they have to take on faith.

## Make the document's own rules explicit

A product doc has conventions — how things are numbered, structured, cross-referenced — and those
conventions should be written down in the doc itself, not left for a new contributor to reverse-
engineer from examples. Pipeline's catalog has a short "Conventions" section right after the
mission for exactly this: designator permanence, one-file-per-feature, margin cross-references
instead of bullet lists. It's a small upfront cost that saves every future contributor from
guessing, or worse, establishing a second inconsistent convention next to the first.

## Give the document an owner and a version

A product doc without an owner or a revision marker reads as either abandoned or perpetually
in-progress — a reader can't tell if what they're looking at is current or three reorgs stale.
Pipeline's catalog carries `doc-id`, `revision`, `date`, `status` ("Current" vs. superseded), and
`owner` as metadata on the document itself, the same way a spec or an RFC would. Bump the revision
and date when the document changes in a way a returning reader should notice — not on every typo
fix, but whenever the shape of the product itself moved.

## Structure for the reader who wants to understand the product today

Group by domain — what a feature is *about* — not by the order things happened to ship in.
Pipeline's catalog is organized into Parts (Core Application, Storage & Sync, MCP Integration,
Services) precisely so someone can read one Part and come away understanding one coherent slice of
the product, without needing the shipping history to make sense of the order. Chronology still
matters — that's what the permanent `PL-NNN` designators are for — but chronology and reading
order are different axes, and a product doc should optimize for the second.

## Shipped, Planned, and "just an idea" are three different things — only two belong here

A product doc should say what's true and what's genuinely decided, not everything that's ever been
discussed. `Shipped` and `Planned` both belong, clearly marked as which. A feature nobody has
actually committed to isn't `Planned`, it's an idea, and belongs in a backlog or a notes doc — not
in the same document a reader is trusting to tell them what the product actually is and is
becoming. Mixing the two trains readers to stop trusting the document's status markers at all,
which defeats the point of having them.

## Prune, don't just accumulate

A product doc that only ever grows eventually describes no product a reader can hold in their
head. When a feature is cut, mark it — don't silently delete the entry (the designator stays
permanent even for a dead feature) and don't leave it looking current. Revisit sections that have
gone stale the same way you'd revisit code that no longer matches what it claims to do.

## Review it like code, because it drifts like code

None of the above holds if the document isn't kept in sync with the product as it actually
changes. Whatever format you use, the document needs to go through the same review the code does —
in a diff, attached to the change that makes it true — or it silently decays into aspirational
fiction within a few months. That's the actual reason Pipeline's catalog is Typst source under
version control instead of a wiki page: not the tool, but the discipline the tool enforces.
