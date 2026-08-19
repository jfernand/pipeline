#import "../template.typ": feature

#feature(
  designator: "PL-010",
  name: "Follow-ups",
  status: "Planned",
  summary: [
    A "Follow-ups" nav rail item exists on tablet. It routes to a screen. The screen says
    "Follow-ups — coming soon." Nothing behind it.
  ],
  purpose: [
    Which applications need action today — an overdue next-action date, a reminder — is a real
    question, not answered by the list alone. This entry exists because the nav item is already
    live and the feature isn't: the gap needs tracking, not hiding.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/nav/Routes.kt — FollowUpsRoute",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/PipelineApp.kt — PlaceholderPane(\"Follow-ups\")",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/OverdueBanner.kt — the closest existing building block (per-application overdue indicator)",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]),),
)
