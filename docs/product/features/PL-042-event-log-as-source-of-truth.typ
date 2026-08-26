#import "../template.typ": feature

#feature(
  designator: "PL-042",
  name: "Event Log as Source of Truth",
  status: "Shipped",
  release: "MVP",
  summary: [
    The Room cache tables (`applications`, `status_events`, `contacts`, `reminders`) are gone.
    `ApplicationStateStore` is now `InMemoryApplicationStateStore` on every platform — never
    itself persisted, materialized by replaying `event_envelopes` (PL-011) through a new
    `replayApplicationState` on first access. The event log becomes the only thing this app
    durably stores, and the only thing a schema change ever has to stay compatible with.
  ],
  purpose: [
    Room used to hold two things in one `@Database`/version: the event log (append-only, stable)
    and a hand-maintained read cache that nothing rebuilt from it — every mutation wrote to both,
    independently. Because they shared one database, the only migration story
    (`fallbackToDestructiveMigration`) wiped both together on any schema bump — a cache-table
    change would have destroyed real user data *and* the log that could have recovered it, in the
    same stroke. Versioning the cache schema and writing per-version migration scripts would only
    have multiplied the surface for that same class of bug.

    Making the log the sole source of truth turns a schema change into an additive-only concern
    (new event fields default so old payloads still decode, the same discipline PL-033 already
    established for `EventProvenance`) instead of a database migration. It also fixed two live
    bugs: js/wasmJs lost real application data on every page reload (its in-memory store wasn't
    replay-based), and every event after an application's first carried the wrong `applicationId`
    — synthesized from the Room row id instead of reused from the original create event — which
    would have silently broken replay's ability to group an application's history back together.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ReplayApplicationState.kt — folds the whole log through applyEvent, tolerant of a malformed or orphaned envelope",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationState.kt, ApplicationEventReducer.kt — applicationId carried on state, fixing the create/edit id-correlation bug",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/InMemoryApplicationStateStore.kt — the one ApplicationStateStore, every platform, lazily materialized from the event log",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/EventSourcedJobApplicationRepository.kt — touches the store before appending, so its lazy materialization never double-counts the event it's about to also write",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/db/AppDatabase.kt — entities down to just EventEnvelopeEntity; a hand-written Migration(3, 4) drops the cache tables without touching event_envelopes",
    "shared/src/jvmTest/kotlin/org/cr/pipeline/data/db/AppDatabaseMigration3To4Test.kt, shared/src/jvmTest/kotlin/org/cr/pipeline/data/RoomEventLogRestartTest.kt — real on-disk SQLite proof: the migration preserves the log, and a real restart recovers real data",
  ),
  related: (("PL-011", [Event-Sourced Local Storage]), ("PL-004", [Local Application Database]), ("PL-033", [Event Provenance])),
)
