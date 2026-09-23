#import "../template.typ": feature

#feature(
  designator: "PL-021",
  name: "Remember Last Route",
  status: "Shipped",
  release: "MVP",
  summary: [
    On launch, the app returns to whatever screen it was on when it closed — not always the list.
    Stored in preferences, restored before the first frame the user sees.
  ],
  purpose: [
    Closing on an application's detail screen and reopening to the list, every single time, is a
    small tax paid on every launch. Preferences already persist device settings (PL-012); a route
    is one more value.
  ],
  description: [
    Scoped to the two screens worth reopening into: `AppPreferences.lastDetailApplicationId` is
    `null` when the list was the last screen (or the app has never run), or an application id when
    its detail screen was. Settings, Add/Edit, Sync, Pair, Follow-ups, and Dev Tools are all
    transient — navigating through them leaves the last-saved value untouched rather than
    clobbering it with somewhere not worth resuming into.

    Restored the same way an incoming deep link already was (PL-009): a `LaunchedEffect` inside
    `PipelinePhoneApp`/`PipelineTabletApp`, in the same composition pass as `NavHost`, navigating
    to the saved `DetailRoute` on top of the always-present `ListRoute` start destination — not by
    overriding `startDestination` itself, which would leave Detail as the graph's root with nothing
    beneath it for Back to pop to. An explicit deep link wins over the saved route when both are
    present, since a deep link reflects something that just happened, not where the user idly left
    off.

    `PreferencesStore` gained a synchronous `currentPreferences` snapshot alongside the existing
    `Flow` — the launch-time redirect needs the saved value before a coroutine gets a chance to
    run, which `observePreferences().collectAsState(...)` can't give without a one-frame flash back
    to the list. Safe because the real implementation always has it ready: reading `Settings` is
    itself synchronous, and happens at construction, not on first collection.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/AppPreferences.kt — lastDetailApplicationId",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/PreferencesStore.kt, SettingsPreferencesStore.kt — currentPreferences, setLastDetailApplicationId",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/PipelinePhoneApp.kt, ui/PipelineApp.kt — the launch-time redirect and the per-navigation persistence LaunchedEffects",
  ),
  related: (
    ("PL-002", [Browse Applications (List & Detail)]),
    ("PL-009", [In-App Navigation & Deep Links]),
    ("PL-012", [App Preferences & Developer Mode]),
    ("PL-043", [Remember List Filter]),
  ),
)
