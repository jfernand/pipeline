#import "../template.typ": feature

#feature(
  designator: "PL-014",
  name: "Export / Import Applications",
  status: "Shipped",
  release: "MVP",
  summary: [
    Export writes every application to a JSON file — versioned `ExportedApplications`. Import reads
    one back and adds each entry as new: additive, never overwrites. JVM has the real
    implementation, native file dialogs, two rows in Settings. Other platforms: a stub.
  ],
  purpose: [
    Local-first storage (PL-004, PL-011) means the data lives on one device. Export is the escape
    hatch — backup, new machine, handing a friend your tracker as a template — until PL-003 ships
    real sync. Import is what makes a second device fast to set up instead of re-entered by hand.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/io/DataPortController.kt — contract (export/import)",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/io/DataPortController.jvm.kt — real implementation, native file dialogs",
    "shared/src/commonMain/kotlin/org/cr/pipeline/di/DataPortModule.kt — Koin wiring",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/phone/SettingsScreen.kt — Export/Import rows",
  ),
  related: (("PL-004", [Local Application Database]), ("PL-012", [App Preferences & Developer Mode])),
)
