#import "../template.typ": feature

#feature(
  designator: "PL-011",
  name: "Event-Sourced Local Storage",
  status: "Shipped",
  release: "MVP",
  summary: [
    Every mutation — create, edit, status change — is a domain event. Events hash into an
    append-only chain (`EventEnvelope`, self-contained multiplatform SHA-256), persisted to Room
    (Android/JVM/iOS) or `localStorage` (js/wasmJs) — the only thing this app durably stores.
    `replayApplicationState` folds the whole chain through the `applyEvent` reducer to materialize
    current state on first access; `AppStatus` is append-only by convention (never remove or
    rename an entry) since there's no runtime fallback for a name a build doesn't recognize the
    way `EventProvenance.Unknown` (PL-033) covers provenance. Local only: no network transport
    moves an event between devices yet.
  ],
  purpose: [
    Two devices with hash-chained histories can find exactly where they diverge and merge without
    asking a server to referee — that is what PL-003 will stand on. It does not need to ship for
    this to already be worth having: a durable, ordered history beats an overwritten row on its
    own, and it means a change to how state is *shaped* is a matter of the reducer and event types
    staying additive, not a database migration.
  ],
  implementation: (
    "sync-core/src/commonMain/kotlin/org/cr/pipeline/sync/chain/EventEnvelope.kt, Hash.kt, Sha256.kt",
    "sync-core/src/commonMain/kotlin/org/cr/pipeline/sync/chain/ChainDiff.kt, ChainStatus.kt",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ — ApplicationEvent, applyEvent reducer, replayApplicationState, EventLog",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/EventSourcedJobApplicationRepository.kt — proxy over storage",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/DeviceIdentityStore.kt — per-device identity, standardized across platforms via Settings",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/RoomEventLog.kt, shared/src/webMain/kotlin/org/cr/pipeline/data/BrowserEventLog.kt — disk persistence",
  ),
  related: (("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)]), ("PL-004", [Local Application Database])),
)
