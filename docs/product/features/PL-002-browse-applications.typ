#import "../template.typ": feature

#feature(
  designator: "PL-002",
  name: "Browse Applications (List & Detail)",
  status: "Shipped",
  release: "MVP",
  summary: [
    The list of tracked applications, and a detail view per application. One navigation graph, two
    layouts: phone gets a single pane, list to detail; tablet gets a persistent nav rail. Window
    width decides which. Nothing else gets a vote.
  ],
  purpose: [
    "Where do things stand" has to have an answer, and this is it. Every other feature — add,
    edit, status update, search — either opens from here or lands back here when it's done.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/App.kt — BoxWithConstraints width-based layout switch",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/ListScreen.kt — phone list",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/DetailScreen.kt — phone detail",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/ListPane.kt — tablet list",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/DetailPane.kt, TabletDetailScreen.kt — tablet detail",
  ),
  related: (("PL-001", [Design System Foundation]), ("PL-004", [Local Application Database]), ("PL-008", [Search & Filter Applications])),
)
