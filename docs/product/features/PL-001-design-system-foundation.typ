#import "../template.typ": feature

#feature(
  designator: "PL-001",
  name: "Design System Foundation",
  status: "Shipped",
  summary: [
    No stock Material widgets. Three vendored typefaces — Barlow Condensed, Space Grotesk, IBM
    Plex Mono — color, typography, and border tokens, and one shared library of primitives: chips,
    cards, buttons, timeline, top bar, FAB, form field, settings row. Every screen on every
    platform draws from the same set. There is no second set.
  ],
  purpose: [
    A database with a UI bolted on looks like one. A product does not. That difference is this
    layer, and it is why every later screen ships fast: nobody re-invents a card, or argues about
    what a status chip should look like, because there is only one.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/theme/PlColors.kt — color tokens",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/theme/PlText.kt — typography",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/theme/PipeModifiers.kt — border/layout modifiers",
    "shared/src/commonMain/composeResources/font/ — vendored Barlow Condensed, Space Grotesk, IBM Plex Mono (OFL)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/ — AppCard, Buttons, StatusChip, Timeline, TopBar, Fab, Field, SettingsRow, etc.",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]),),
)
