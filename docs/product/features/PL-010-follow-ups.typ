#import "../template.typ": feature

#feature(
  designator: "PL-010",
  name: "Follow-ups",
  status: "Shipped",
  release: "MVP",
  summary: [
    The "Follow-ups" nav rail tab, tablet only, now lists every overdue next-action date and
    overdue reminder across the whole pipeline in one place — sorted by due date, each row tapping
    straight through to the application it belongs to.
  ],
  purpose: [
    Which applications need action today — an overdue next-action date, a reminder — is a real
    question, not answered by the list alone: the list's own "Needs follow-up" section only
    surfaces the next-action-date signal, and per-application reminders were otherwise only
    visible one detail screen at a time. One screen, sorted by urgency, answers it directly.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/FollowUpItem.kt — the flattened row shape",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationStateMapping.kt — toFollowUpItems, the merge/sort logic",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt, EventSourcedJobApplicationRepository.kt — observeFollowUps",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/FollowUpsContent.kt — the screen",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/PipelineApp.kt — FollowUpsRoute wiring, replacing PlaceholderPane",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]),),
)
