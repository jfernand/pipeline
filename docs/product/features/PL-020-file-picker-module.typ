#import "../template.typ": feature

#feature(
  designator: "PL-020",
  name: "File Picker Module",
  status: "Planned",
  summary: [
    `DataPortController.jvm.kt`'s private `showFileDialog` — a thin wrapper over
    `java.awt.FileDialog` — pulled out into its own module. One file picker, reused by
    export/import and by PL-018's document attachments, instead of two copies drifting apart.
  ],
  purpose: [
    PL-018 needs a file picker. One already exists, buried inside `DataPortController.jvm.kt`
    where only export/import can reach it. Extract it once, now — before a second copy gets
    written because the first one wasn't visible from outside its file.
  ],
  implementation: (
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/io/DataPortController.jvm.kt — showFileDialog(), the code this extracts",
  ),
  related: (("PL-014", [Export / Import Applications]), ("PL-018", [Document Attachments (Resume & Cover Letter)])),
)
