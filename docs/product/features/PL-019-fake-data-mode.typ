#import "../template.typ": feature

#feature(
  designator: "PL-019",
  name: "Fake Data Mode",
  status: "Shipped",
  release: "MVP",
  summary: [
    A "Show fake data" switch in Dev Tools. On, the app reads and writes a separate demo event
    chain instead of the real one — seeded once, the first time anything touches it, with one real
    `ApplicationCreated` event per `SeedData` entry; from then on it's an ordinary chain, editable
    and durable exactly like real data. Off, a fresh install now starts genuinely empty — no demo
    content synthesized into memory on every launch the way it used to be.
  ],
  purpose: [
    Testing a UI change against real data risks the real data — Dev Tools needed a sandbox that
    looks exactly like production and can't touch it. It's also what closes this catalog's own
    review's top finding: seed data used to be synthesized fresh into memory on every launch, live
    only until any real event landed, at which point an ordinary edit to it wrote an event an
    empty log had no `ApplicationCreated` for — surviving a restart, the whole app replayed to
    empty. Demo data is now exactly as durable and event-sourced as real data, because as far as
    replay is concerned, it *is* real data — just tagged apart from it.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/EventLog.kt — EventLogKind (REAL/DEMO)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/DemoSeedingEventLog.kt — seeds an empty demo chain exactly once, lazily, on first read or write",
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/SeedData.kt — trimmed to only the fields a real ApplicationCreated event can carry; toApplicationInput",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/InMemoryApplicationStateStore.kt — the old showingSeedData special-case removed entirely; an empty log is just empty now",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/AppPreferences.kt, PreferencesStore.kt, SettingsPreferencesStore.kt — the showFakeData preference",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/db/EventEnvelopeEntity.kt, AppDatabase.kt — logKind column (MIGRATION_4_5), Android/desktop/iOS",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/RoomEventLog.kt — queries scoped by logKind",
    "shared/src/webMain/kotlin/org/cr/pipeline/data/BrowserEventLog.kt — a kind-suffixed localStorage key, web",
    "shared/src/roomMain, jsMain, wasmJsMain/kotlin/org/cr/pipeline/di/PlatformDataModule.*.kt — which chain backs the app, decided once at startup from the persisted preference",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/DevToolsContent.kt — the switch",
  ),
  related: (
    ("PL-004", [Local Application Database]),
    ("PL-011", [Event-Sourced Local Storage]),
    ("PL-012", [App Preferences & Developer Mode]),
    ("PL-042", [Event Log as Source of Truth]),
  ),
)
