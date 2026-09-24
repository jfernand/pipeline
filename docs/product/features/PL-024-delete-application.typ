#import "../template.typ": feature

#feature(
  designator: "PL-024",
  name: "Delete Application",
  status: "Shipped",
  release: "MVP",
  summary: [
    Removes an application outright. Swipe left to reveal a trash can on phone; an Edit-style
    icon button plus a confirm sheet on tablet and phone's detail header. A context menu and the
    Delete key on desktop are deferred — out of scope for this pass.
  ],
  purpose: [
    Every application PL-006 can create currently outlives its usefulness with no way back out —
    a mis-added entry, a withdrawn application, sits in the list forever. Add and Edit have always
    had a counterpart for undoing themselves. Delete has not, until this.
  ],
  description: [
    A soft delete, not a hard one — consistent with PL-042 making the event log the sole source of
    truth. A new `ApplicationDeleted` event marks `ApplicationState.deleted = true`; nothing is
    removed from `event_envelopes`, so the full history an application had survives even after it
    stops showing up anywhere. `EventSourcedJobApplicationRepository` filters a deleted state out
    of every read path — list, search (PL-008), detail, follow-ups — so a deleted id reads exactly
    like an unknown one, including through the MCP tools, which go through the same paths.

    Two doors, two amounts of friction. The phone list's `AppCard` is wrapped in a Material3
    `SwipeToDismissBox` (`ListScreen.kt`): swipe left reveals a red background with a trash icon,
    and a full swipe commits the delete directly — no separate confirmation, since the gesture
    itself is deliberate enough. The detail header's Delete icon (now wired on phone, newly added
    on tablet, which had no delete affordance in its header at all) is a single tap, easy to
    trigger by accident, so it opens `DeleteConfirmSheet` — the same bottom-sheet shape as
    `StatusSheet`/`ContactSheet` — before doing anything. Confirming there also pops back to the
    list, since a deleted application has nothing left to keep showing in a detail view.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationEvent.kt, ApplicationState.kt, ApplicationEventReducer.kt, ReplayApplicationState.kt — ApplicationDeleted, ApplicationState.deleted",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt, EventSourcedJobApplicationRepository.kt — deleteApplication, deleted filtered out of every read path",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/ListScreen.kt — SwipeToDeleteAppCard, the swipe-to-reveal-trash gesture",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/screens/DeleteConfirmSheet.kt — the tap-to-delete confirm sheet, shared by phone and tablet",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/DetailScreen.kt, ui/phone/PipelinePhoneApp.kt — phone detail header's Delete icon, now wired",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/DetailPane.kt, ui/tablet/TabletDetailScreen.kt, ui/PipelineApp.kt — tablet detail header's new Delete icon button",
  ),
  related: (
    ("PL-006", [Add Application]),
    ("PL-004", [Local Application Database]),
    ("PL-042", [Event Log as Source of Truth]),
  ),
)
