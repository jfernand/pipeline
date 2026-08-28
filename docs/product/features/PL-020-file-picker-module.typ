#import "../template.typ": feature

#feature(
  designator: "PL-020",
  name: "File Picker Module",
  status: "Shipped",
  release: "Backlog",
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
  description: [
    `showFileDialog` itself moved out of `DataPortController.jvm.kt` into its own file,
    `FileDialogs.jvm.kt` — same function, `internal` instead of `private`, with the `.json`
    extension-forcing that only export ever needed left in `DataPortController.jvm.kt` rather than
    baked into the shared dialog call. `FilePicker` is the commonMain-facing interface PL-018's
    Add/Edit form actually depends on (`pickFile(): PickedFile?`, `isSupported`) — its JVM
    implementation is the only caller of `showFileDialog` outside `DataPortController` itself.

    Only JVM (desktop) has a real implementation, same rollout shape as
    `DataPortController`/`FileArchiveService`: Android/iOS/js/wasmJs each get
    `UnsupportedFilePicker` until one of them gets a real picker of its own.
  ],
  implementation: (
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/io/FileDialogs.jvm.kt — showFileDialog(), extracted from DataPortController.jvm.kt",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/io/FilePicker.kt — the picker interface, PickedFile, UnsupportedFilePicker",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/io/FilePicker.jvm.kt — the real, JVM-only implementation",
    "shared/src/androidMain, iosMain, jsMain, wasmJsMain/kotlin/org/cr/pipeline/data/io/FilePicker.*.kt — UnsupportedFilePicker on every platform without a real one yet",
    "shared/src/commonMain/kotlin/org/cr/pipeline/di/FilePickerModule.kt",
  ),
  related: (("PL-014", [Export / Import Applications]), ("PL-018", [Document Attachments (Resume & Cover Letter)])),
)
