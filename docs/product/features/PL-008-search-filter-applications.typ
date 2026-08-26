#import "../template.typ": feature

#feature(
  designator: "PL-008",
  name: "Search & Filter Applications",
  status: "Shipped",
  release: "MVP",
  summary: [
    Live text search over company and role. Status filter chips — Wishlist, Applied, Screen, and
    the rest, each independently required/excluded/not-set (PL-023). Same list narrows on both
    phone and tablet as you type or tap a chip.
  ],
  purpose: [
    Four applications, the list (PL-002) is enough on its own. Forty, and "where do things stand"
    needs an answer faster than scrolling. This is that answer.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/SearchField.kt — PlSearchField",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/StatusFilterChips.kt — status chips + rememberApplicationListFilter",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/ListScreen.kt — phone wiring",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/ListPane.kt — tablet wiring",
  ),
  related: (("PL-002", [Browse Applications (List & Detail)]), ("PL-023", [Negative Search Filters])),
)
