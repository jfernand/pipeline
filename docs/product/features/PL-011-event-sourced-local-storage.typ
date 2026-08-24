#import "../template.typ": feature

#feature(
  designator: "PL-011",
  name: "Event-Sourced Local Storage",
  status: "Shipped",
  release: "MVP",
  summary: [
    Every mutation — create, edit, status change — is a domain event. Events hash into an
    append-only chain (`EventEnvelope`, self-contained multiplatform SHA-256). A single
    `applyEvent` reducer replays the chain into current state. Device identity and the event log
    persist to Room, or `localStorage` on js/wasmJs. Local only: no network transport moves an
    event between devices yet.
  ],
  purpose: [
    Two devices with hash-chained histories can find exactly where they diverge and merge without
    asking a server to referee — that is what PL-003 will stand on. It does not need to ship for
    this to already be worth having: a durable, ordered history beats an overwritten row on its
    own.
  ],
  implementation: (
    "sync-core/src/commonMain/kotlin/org/cr/pipeline/sync/chain/EventEnvelope.kt, Hash.kt, Sha256.kt",
    "sync-core/src/commonMain/kotlin/org/cr/pipeline/sync/chain/ChainDiff.kt, ChainStatus.kt",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ — ApplicationEvent, applyEvent reducer, EventLog",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/EventSourcedJobApplicationRepository.kt — proxy over storage",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/DeviceIdentityStore.kt — per-device identity",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/RoomEventLog.kt, RoomDeviceIdentityStore.kt — disk persistence",
  ),
  related: (("PL-003", [Device Pairing & Multi-Device Sync (UI Scaffold)]), ("PL-004", [Local Application Database])),
)
