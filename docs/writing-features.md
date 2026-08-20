# Writing good feature entries

This is for whoever is actually filling in a `feature()` block — summary, purpose, status,
implementation, related — not the file layout. For the file structure, designator numbering, and
build commands, see [docs/feature-catalog.md](feature-catalog.md). For the product-level
questions — mission, scope, how the document as a whole is structured and governed — see
[docs/defining-the-product.md](defining-the-product.md). This is about writing one entry well once
you're already in it.

## Summary and Purpose are different jobs — don't blend them

**Summary** is what a reader encounters: the feature, described plainly, as if explaining it to
someone using the app. **Purpose** is why it's worth the app having: what it serves, tied back to
the mission.

Pipeline's mission is one sentence: *"Pipeline tracks job applications. Company, role, status,
dates, contacts, follow-ups. Nothing else, and nothing less."* Every purpose paragraph in the
catalog should trace back to that sentence in one hop. If you can't make the connection in a
sentence or two, that's not a sign the purpose needs more words — it's a sign the feature might
not belong, or you haven't found the real reason yet.

Compare:

> Purpose: This will help users manage their documents better.

against the actual PL-018 entry:

> Purpose: Which résumé went to which company stops being a mental note, or a folder named
> "resume-v3-final-actually," and becomes part of the record — where it belonged from the start.

The first is true of almost any feature and says nothing. The second names the specific problem
(the mental note, the folder full of `-final-actually` files) and the specific fix. That
specificity is the test of whether you actually know why you're building something, or just have
a vague sense that it'd be nice.

## Status means what it says

`Shipped` means the code exists and does the thing. `Planned` means it's real, decided intent —
not a maybe, not a "we were kicking this around." If you're not confident enough in a feature to
say it's actually going to happen, it doesn't belong in the catalog yet; write it down somewhere
else until it firms up. A catalog padded with half-considered ideas trains readers to stop trusting
`Planned` as a signal.

Don't round up, either. A feature with one exploratory branch and no committed design is not
`Shipped`, and describing it as if it works because most of it works is worse than admitting it
doesn't — the reader who trusts the catalog is the one who gets burned.

## Implementation: point at code, not intentions

The implementation list is file paths with a one-line note, not a paragraph. It exists to answer
"where do I look" for someone who already believes the feature exists and wants to see it. It is
not a place to describe what you're *going to* build — that's what `purpose` already covers.

For a `Planned` feature with no code yet, an empty list is the honest answer. Don't fill it with
"will use Room database for storage" — that's a design decision dressed up as evidence, and it's
usually wrong anyway once the code actually gets written. Nothing to point to yet is itself
information; let the entry say that by leaving the section out rather than faking substance.

## Related features: real coupling, not free association

Cross-reference another designator when a reader would genuinely need to jump there to understand
this entry — not because the two features happen to touch the same screen, or because you can
technically draw a line between them. Ask: if this entry didn't mention that one, would the reader
walk away with a wrong idea? If the answer's no, leave it out.

When the coupling runs both ways — a feature and the service that exists specifically to support
it — reference it from both sides. Don't force the reverse when it doesn't: a feature citing the
design system it's built on doesn't obligate the design system's own entry to list every feature
that uses it, or it'd end up linking to half the catalog.

## Voice: write like you already believe it

Terse, declarative sentences. No hedging (*might*, *could potentially*, *may help*), no marketing
(*powerful*, *seamless*, *effortless*), no throat-clearing before the point. Say the thing, then
stop.

> Every hardcoded English string is a decision the next translation has to work around instead of
> through. The later this starts, the more of those decisions there are to undo.

That's the whole purpose section for PL-030 — two sentences, no wasted motion, and it's clear
exactly why the feature matters and what happens if it's delayed. That's the bar.

## Write for someone who wasn't in the room

No "as discussed," no unexplained internal shorthand, no assuming the reader remembers which
Slack thread this came out of. A catalog entry has to stand alone for someone reading it a year
from now, cold. If an acronym or a reference needs unpacking, unpack it once, in the entry, or
don't use it.

## The designator is the feature's real name

The `name:` field can be refined later — a better phrase, a clearer word. The `PL-NNN` designator
can't; it's permanent the moment it's assigned, even to a feature that later gets cut. Write and
speak about features by designator once one exists (*"PL-018 needs..."*, not *"the attachments
thing needs..."*) — that's what keeps the reference stable while everything else about the
feature, including its name, stays negotiable.
