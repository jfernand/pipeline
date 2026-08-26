#import "../template.typ": feature

#feature(
  designator: "PL-004",
  name: "Local Application Database",
  status: "Shipped",
  release: "MVP",
  summary: [
    `JobApplicationRepository`, Koin-injected, backed by one `ApplicationStateStore` on every
    platform: an in-memory projection, materialized by replaying `event_envelopes` (PL-011) at
    first access — nothing here is itself durable. Seeded with QA data whenever that log is empty
    — the app is never empty on a fresh install.
  ],
  purpose: [
    No storage, no tracking — that is the whole point of this layer. Every read and every write
    the app makes, from the list screen to an MCP tool call, goes through it. There is no second
    path.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt — the contract",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/ApplicationStateStore.kt — storage interface",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/InMemoryApplicationStateStore.kt — the one implementation, every platform, materialized from the event log via replayApplicationState",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/db/AppDatabase.kt — Room now holds only event_envelopes; no cache tables to migrate",
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/SeedData.kt — QA seed data shown only while the event log is empty",
    "shared/src/commonMain/kotlin/org/cr/pipeline/di/PlatformDataModule.kt (+ per-platform variants) — Koin wiring",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]), ("PL-011", [Event-Sourced Local Storage])),
)
