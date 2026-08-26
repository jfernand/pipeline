#import "../template.typ": feature

#feature(
  designator: "PL-023",
  name: "Negative Search Filters",
  status: "Shipped",
  release: "MVP",
  summary: [
    A third state on every status filter chip: not set, required, excluded — each with its own
    look (excluded is `PlColors.danger` red, distinct from required's brand amber). Tapping cycles
    not-set → required → excluded → not-set, independently per chip, so any combination can be
    active at once. "Excluded" on Rejected alone reads as "show me everything except Rejected."
  ],
  purpose: [
    "Everything except Rejected" is a real question the old positive-only, single-select filter
    couldn't answer, short of tapping every other chip by hand — and a single-select filter has no
    room for "except" at all, since selecting anything replaced whatever was selected before. One
    chip, three states, kept independent per status, says what a whole row of mutually-exclusive
    chips currently couldn't.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/StatusFilterChips.kt — StatusFilterMode, the required/excluded state map, and the cycle-on-tap logic",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/StatusChip.kt — PlFilterChip's negative visual variant",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/ListScreen.kt, ui/tablet/ListPane.kt — the two call sites, unchanged in structure, just wired to the new state shape",
  ),
  related: (("PL-008", [Search & Filter Applications]),),
)
