#import "../isss-doc.typ": *

#show: isss-doc.with(
  title: "Pipeline",
  subtitle: "Codebase Review — 2026-08-26",
  class: "Engineering Review",
  doc-id: "ISSS-RVW-0001",
  revision: "1.0",
  date: "2026-08-26",
  status: "Final",
  applies-to: "Pipeline — shared, androidApp, desktopApp (working tree at 9b4c202)",
  owner: "Javier Fernández",
  classification: "",
)

#heading(level: 1, numbering: none)[Overview]

This review covers the whole repository, not just the working-tree diff — PL-037 (Artifact
Version from Build Info), PL-039 (Platform App Icons), and PL-023 (Negative Search Filters), in
that order, plus everything already committed underneath them.

Nine parallel review passes ran across the codebase and converged from independent angles on the
same top finding; the highest-severity candidates each pass surfaced were then re-traced by hand
against the actual source (`InMemoryApplicationStateStore.kt`, `EventSourcedJobApplicationRepository.kt`,
`McpApp.kt`, `AppDatabase.kt`, `gradle.properties`) rather than taken on the reviewing agent's word
— every finding below marked "verified" was independently confirmed by reading the exact lines
cited and tracing the call path by hand, not just reported.

// isss-doc.typ's own danger-block() passes a `stroke` dict this Typst version rejects
// (`paint`/`thickness` keys aren't valid alongside a side key) — reproduced here inline with a
// stroke dict this version accepts, rather than patching the vendored template for one report.
#block(
  width: 100%, above: 14pt, below: 14pt,
  fill: panel,
  stroke: (left: 1.5pt + danger),
  inset: (x: 11pt, y: 9pt),
)[
  #label("Critical", color: danger, size: 7pt)
  #linebreak()
  #par(leading: 4.5pt, text(font: body-font, size: 9pt, fill: ink-body)[
    The single most severe finding (C1) is a data-loss bug: on a fresh install, editing or
    changing the status of one of the seven demo applications — the single most natural first
    action in the app — permanently blanks *all* application data on the next app restart, MCP
    server start, or reload. It predates all three requested features; it lives in PL-042 (Event
    Log as Source of Truth), already on `master`. See C1 below.
  ])
]

Fourteen findings total: two Critical, two High, three Medium, seven Low/cleanup. None of them are
in the lines PL-037, PL-039, or PL-023 actually changed — see the next section — but four of the
Low findings are debris left alongside that work (a leftover debug task, a stray export file,
duplicated git-lookup logic) and are worth clearing before it ships.

#heading(level: 1, numbering: none)[PL-037, PL-039, PL-023 — Assessment]

== PL-037 — Artifact Version from Build Info

Functionally correct: `androidApp`'s `versionCode`/`versionName` and `desktopApp`'s
`packageVersion` now derive from `git rev-list --count` / `git describe`, matching
`BuildInfo.GIT_DESCRIBE`. Three issues surfaced, none of them in the version-deriving logic
itself:

- *H2* — the git lookups run at Gradle configuration time with no protection against
  configuration-cache reuse, so a cached configuration can ship a stale version.
- *L1* — a leftover `printGitVersionDebug` debug task in `androidApp/build.gradle.kts`.
- *L3* — the git-lookup helper is now implemented three times (`androidApp`, `desktopApp`,
  and `shared`'s pre-existing `GenerateGitInfoTask`) instead of shared once.

== PL-039 — Platform App Icons

Functionally correct on inspection — Android adaptive icon + legacy mipmaps, iOS
`AppIcon.appiconset`, desktop `.icns`/`.ico`/`.png` wired into both the packaged installer and the
running window (`main.kt`), web favicon. No findings against the icon assets or wiring. One stray
file sitting in the same directory as the desktop icon work is flagged separately:

- *L2* — `desktopApp/pipeline-export-2026-08-25.json`, an untracked data export unrelated to any
  shipped feature, left in `desktopApp/`.

== PL-023 — Negative Search Filters

Clean. The three-state cycle (not-set → required → excluded → not-set) is implemented once, in
`rememberApplicationListFilter`, with `StatusFilterMode` kept as UI-only state — never written to
the event log. `StatusFilterChipsTest.kt` covers the unset, single-positive, single-negative,
multi-positive-OR, and multi-negative-exclude cases. No findings.

#heading(level: 1, numbering: none)[Critical Findings]

Both of these are pre-existing — neither was introduced by PL-037, PL-039, or PL-023 — but both
surfaced only because this pass covered the whole repository, which is exactly what widening the
review from the diff to the codebase was for.

== C1 — Seed edits wipe the app

#note[Verified: traced end to end against `InMemoryApplicationStateStore.kt`,
  `EventSourcedJobApplicationRepository.kt`, and `ReplayApplicationState.kt`.]

*Where:* `shared/src/commonMain/kotlin/org/cr/pipeline/data/InMemoryApplicationStateStore.kt:94–113`,
`EventSourcedJobApplicationRepository.kt:48–59`.

A fresh install shows seven seed applications. They're demo content, generated fresh on every
launch and never backed by a real `ApplicationCreated` event — each one gets a throwaway random
`ApplicationId` the moment it's materialized (`InMemoryApplicationStateStore.kt:118`), not one
tied to anything in the (empty) event log.

`write()`'s create path (`id == null`) correctly notices this and clears seed mode:

// Reproduced from isss-doc.typ's listing() with a stroke dict this Typst version accepts — see
// the note above the earlier block for why this isn't just a call to listing() directly.
#block(
  width: 100%, above: 16pt, below: 5pt,
  fill: panel,
  stroke: (left: 1.5pt + amber),
  inset: (x: 12pt, y: 10pt),
  text(font: mono-font, size: 8.5pt, fill: ink-body)[
```kotlin
override suspend fun write(id: Long?, state: ApplicationState): Long {
    ensureMaterialized()
    if (id == null) {
        if (showingSeedData) {
            records.value = emptyList()
            nextId = 1L
            showingSeedData = false
        }
        val newId = nextId++
        records.update { list -> listOf(Record(newId, state)) + list }
        return newId
    }
    records.update { list -> list.map { record -> if (record.id == id) record.copy(state = state) else record } }
    return id
}
```
  ],
)
#block(below: 16pt, label[Listing 1 — `write()` — the create branch clears seed mode; the edit branch does not.])

The `id != null` branch — every edit and every status change — does not. Nothing does: it's the
only other path through `write()`. So the first time a user's first action is to edit a seed
application, or change its status, instead of adding a new one:

+ `EventSourcedJobApplicationRepository.saveApplication`/`updateStatus` reads the seed record's
  random `applicationId` and durably appends a real `ApplicationEdited`/`StatusChanged` event to
  the event log, referencing an application that was never created.
+ `write()` patches the in-memory record in place. `showingSeedData` stays `true`. Nothing on
  screen looks wrong.
+ On the next materialization — app restart, page reload, or the MCP server touching the same
  store from a different process — `ensureMaterialized()` sees a non-empty log, so it does *not*
  fall back to seed data.
+ `replayApplicationState` finds an edit/status-change event with no prior `ApplicationCreated`
  for its `applicationId` and skips it as an orphan (`ReplayApplicationState.kt:56`) — and nothing
  else in the log has ever created anything.
+ `records.value` materializes to an empty list. All seven seed applications are gone, not just
  the one that was edited, and there is no way back — the orphan event is real and durably
  written; only the projection was lost.

This violates the "app is never empty" invariant `InMemoryApplicationStateStore`'s own class doc
states outright. No existing test (`InMemoryApplicationStateStoreTest`, `ReplayApplicationStateTest`)
exercises an edit-with-no-prior-create path.

*Fix shape:* the `id != null` branch needs the same seed-clear-and-reseed-numbering behavior the
`id == null` branch already has — or, better, `saveApplication`/`updateStatus` should route an
edit of a still-seed-showing application through the *create* path (a real `ApplicationCreated`
first), since a seed record has no real create event to attach an edit to.

== C2 — v1/v2 devices lose all data

#note[Verified: `AppDatabase.kt` registers only `Migration(3, 4)`;
  `fallbackToDestructiveMigration(dropAllTables = true)` covers everything else.]

*Where:* `shared/src/roomMain/kotlin/org/cr/pipeline/data/db/AppDatabase.kt` — `version = 4`, one
registered migration (`Migration(3, 4)`), `fallbackToDestructiveMigration(dropAllTables = true)`.

The `Migration(3, 4)` comment says it exists specifically so this release doesn't destructively
wipe `event_envelopes` — but it only covers upgrading *from* version 3. A device still sitting at
schema version 1 or 2 (any install that hasn't opened the app since before the version-3
`device_identity` migration) has no migration path to 4 at all, so
`fallbackToDestructiveMigration` drops every table, including the durable event log this release
was specifically written to protect.

Real-world exposure is limited today — the product catalog states no release has shipped with a
version tag yet — but the code has no guard against it, and the gap is easy to miss precisely
because `Migration(3, 4)` reads like the problem is already handled.

*Fix shape:* either add real `Migration(1, 4)`/`Migration(2, 4)` paths, or — if pre-v3 installs
are known not to exist in practice — assert that in a comment next to the fallback so the gap is a
documented decision, not a silent one.

#heading(level: 1, numbering: none)[High-Severity Findings]

== H1 — MCP reports false success

#note[Verified against `EventSourcedJobApplicationRepository.kt:53` and `McpApp.kt:96–104`.]

*Where:* `EventSourcedJobApplicationRepository.kt:53`, `shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt:96–104`.

`saveApplication`'s edit path is `val current = store.getState(id) ?: return id` — if `id` doesn't
exist (a stale id, or an id wiped out by C1), it silently returns without appending an event or
changing anything. `McpApp`'s `editApplication` handler calls this, discards the return value
entirely, and unconditionally replies `ToolResponse.Ok("Updated application #$id: ...")`. An agent
or user editing a non-existent application over MCP is told the edit succeeded when nothing
happened. (The pre-PL-042 code crashed on this instead via `checkNotNull` — a regression in kind,
not degree, but a caller that trusts the response is worse off with a false success than a crash.)

*Fix shape:* have `saveApplication` signal "not found" distinguishably (nullable return, a sealed
result, or a thrown exception the MCP layer catches), and have the MCP handler reply with an error
tool response instead of `Ok` when that happens.

== H2 — Versions aren't cache-safe

#note[`gradle.properties:6` confirms `org.gradle.configuration-cache=true` is this repo's default,
  not an opt-in edge case.]

*Where:* `androidApp/build.gradle.kts:12–28`, `desktopApp/build.gradle.kts:13–29`.

`gitVersionCode`/`gitVersionName` (Android) and `gitPatchVersion`/`gitPackageVersion` (desktop)
are computed by calling `providers.exec` directly in top-level script code — at configuration
time, not inside a task — with no declared input tying that result to the current git state. When
the configuration cache is reused across builds, Gradle can skip configuration entirely and reuse
the previously computed version, even though new commits landed or the tree's dirty/clean state
changed in between. The shipped artifact's version then silently lags reality.

The codebase already has the fix for this exact hazard, just not applied here: `shared/build.gradle.kts`'s
`GenerateGitInfoTask` — which generates the same underlying `BuildInfo.GIT_DESCRIBE` string —
forces `outputs.upToDateWhen { false }`, with a comment calling out precisely this ("git HEAD/dirty
state can change without any other Gradle input changing"). Neither `androidApp/build.gradle.kts`
nor `desktopApp/build.gradle.kts` replicates that safety property. The repository's own empty
`throwaway: config-cache staleness test` commit (`9b4c202`, currently at `HEAD`) is consistent with
this having already been under investigation, unresolved.

*Fix shape:* move the git lookups into a task (or a custom `ValueSource`) with the same
`upToDateWhen { false }` / non-cacheable treatment `GenerateGitInfoTask` already uses, so a
configuration-cache hit can't paper over a real git-state change.

#heading(level: 1, numbering: none)[Medium-Severity Findings]

== M1 — Unsynchronized id allocation

*Where:* `InMemoryApplicationStateStore.kt:94–113`.

`materializeMutex` guards `ensureMaterialized()` only. `write()` takes no lock, but the class's own
doc comment says both the Compose UI and the MCP server — a genuinely different thread, confirmed
via `McpApp.kt`'s `runBlocking` calls on Netty request threads — are expected to touch this
singleton concurrently. Two concurrent creates can read the same `nextId` before either
increments it, producing two records sharing an id; `getState`/`observeState` (`.find { it.id ==
id }`) then silently resolve to only one of them.

*Fix shape:* bring `write()`'s critical section under the same lock `ensureMaterialized()` uses.

== M2 — No atomicity in persist()

*Where:* `EventSourcedJobApplicationRepository.kt:72–80`.

Two concurrent mutations on the same application (e.g. a status change from the UI racing an edit
from MCP) both read the same starting state before either persists. Both append their own event to
the durable log correctly — the log itself stays consistent — but each computes the new projected
state from the same stale starting point, so whichever `store.write()` call lands last silently
overwrites the other's projected result. The event log has both events; the in-memory projection
reflects only one, until the next full replay.

*Fix shape:* serialize `persist()` per application (or globally, given current volumes) rather
than relying on the two independent operations it's built from staying accidentally consistent.

== M3 — Non-atomic materialized flag

*Where:* `InMemoryApplicationStateStore.kt:54–76`.

`ensureMaterialized()`'s fast-path check (`if (materialized) return`) reads a plain, non-volatile
`Boolean` with no happens-before relationship to the write inside `materializeMutex.withLock`.
Textbook double-checked-locking pitfall; worst case on this specific field is redundant re-replay
work rather than corruption, but it should be `@Volatile` or an atomic, not a plain `var`.

#heading(level: 1, numbering: none)[Low-Severity / Cleanup Findings]

#data-table(
  columns: (auto, auto, 1fr),
  header: ("ID", "Where", "Finding"),
  [L1], [androidApp/build.gradle.kts:28], [Leftover debug task `printGitVersionDebug` — never invoked by any other task or CI step; has no `desktopApp` equivalent despite computing the same kind of value. Delete before shipping PL-037.],
  [L2], [desktopApp/pipeline-export-2026-08-25.json], [Untracked manual-test data export, unrelated to any shipped feature, sitting directly in desktopApp/. Delete or move out of the repo.],
  [L3], [androidApp + desktopApp build.gradle.kts], [The git describe/rev-list lookup helper is now implemented three separate times — androidApp, desktopApp, and shared's GenerateGitInfoTask — in two different Gradle API styles. A future change to the lookup has to be applied by hand in three places, exactly the kind of drift PL-037 exists to eliminate.],
  [L4], [EventSourcedJobApplicationRepository.kt:77], [persist() forces materialization by calling and discarding store.observeAll().first(), a correctness-critical ordering step documented only in a comment, not in ApplicationStateStore's interface. A future write path (bulk import, sync merge) can append to the event log without it and silently double-count on first materialization.],
  [L5], [InMemoryApplicationStateStore.kt:52], [nextId is hand-maintained mutable state duplicating what records already encodes (three call sites keep it in sync by hand). Since there's no delete operation, it's always exactly `(records.value.maxOfOrNull { it.id } ?: 0L) + 1L` — computing it inline removes one more thing that can drift.],
  [L6], [ApplicationStateStore.kt:13], [Interface doc comment still lists "Room, in-memory" as the two implementations; RoomApplicationStateStore was deleted by the PL-042 diff (confirmed gone from source — only stale compiled .dex artifacts remain under shared/build/). In-memory is now the only implementation.],
  [L7], [androidApp/build.gradle.kts:22], [versionCode from `git rev-list --count HEAD` isn't guaranteed to strictly increase across a history rewrite (rebase, squash-merge) or a maintenance branch with fewer commits than a prior release — Play requires strict monotonicity. Called out in the code's own comment as an accepted stopgap until real release tags exist, so likely intentional rather than an oversight; included for completeness.],
)

#heading(level: 1, numbering: none)[Summary & Recommended Next Actions]

PL-037, PL-039, and PL-023 are each sound in the lines they actually changed — the findings
against them are all cleanup (L1, L2, L3) or a risk in shared infrastructure they lean on (H2), not
bugs in their own logic. PL-023 in particular ships with full test coverage and had nothing
flagged against it at all.

The two Critical findings are the ones that matter most before anything else here ships or gets
used day to day:

+ *C1* is the one to fix first — it's a data-loss bug reachable by the single most ordinary first
  action a new user can take (edit a demo entry instead of adding a new one), and it's already on
  `master` via PL-042.
+ *C2* is lower urgency today (no tagged release exists yet to have shipped a v1/v2 device), but
  cheap to close with an explicit comment or a real migration path, and gets more expensive to
  reason about the longer it's deferred.

Suggested order: C1, C2, H1, H2, then the three Low items directly in the way of committing
PL-037/PL-039 (L1, L2, L3) before those features ship. M1–M3 and L4–L7 are real but lower-stakes —
fine to batch into a follow-up pass rather than blocking on tonight's three features.
