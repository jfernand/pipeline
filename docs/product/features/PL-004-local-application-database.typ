#import "../template.typ": feature

#feature(
  designator: "PL-004",
  name: "Local Application Database",
  status: "Shipped",
  summary: [
    `JobApplicationRepository`, Koin-injected, backed by Room on Android/JVM/iOS and an in-memory
    store on js/wasmJs (Room has no target there). Seeded with QA data on first run — the app is
    never empty.
  ],
  purpose: [
    No storage, no tracking — that is the whole point of this layer. Every read and every write
    the app makes, from the list screen to an MCP tool call, goes through it. There is no second
    path.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt — the contract",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/ApplicationStateStore.kt — expect storage interface",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/RoomApplicationStateStore.kt — Room-backed actual",
    "shared/src/roomMain/kotlin/org/cr/pipeline/data/db/ — entities (Application, Contact, Reminder, StatusEvent) and DAOs",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/InMemoryApplicationStateStore.kt — js/wasmJs actual",
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/SeedData.kt — first-run QA seed data",
    "shared/src/commonMain/kotlin/org/cr/pipeline/di/PlatformDataModule.kt (+ per-platform variants) — Koin wiring",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]), ("PL-011", [Event-Sourced Local Storage])),
)
