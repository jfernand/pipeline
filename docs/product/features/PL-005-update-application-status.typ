#import "../template.typ": feature

#feature(
  designator: "PL-005",
  name: "Update Application Status",
  status: "Shipped",
  release: "MVP",
  summary: [
    A bottom sheet on any application. Moves it to a new status — Applied, Phone Screen,
    Interviewing, Offer — with an optional note. No full edit form needed.
  ],
  purpose: [
    Nothing on an application changes as often as its status. This is the fast path for that, and
    every change it makes appends a timestamped history entry — so what you have is *when* it
    moved, not just where it happens to sit now.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/screens/StatusSheet.kt — the bottom sheet",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/StatusPickerFlowRow.kt — status picker",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt — updateStatus(id, status, note)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/PipelinePhoneApp.kt — StatusSheet wiring (phone)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/PipelineApp.kt — StatusSheet wiring (tablet)",
  ),
  related: (("PL-007", [Edit Application]),),
)
