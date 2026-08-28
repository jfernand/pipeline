#import "../template.typ": feature

#feature(
  designator: "PL-031",
  name: "File Management Service",
  status: "Shipped",
  release: "MVP",
  summary: [
    Owns the on-disk life of every file PL-018 attaches to an application: where it's stored,
    how it's named, and what happens to it when the application — or just the attachment — is
    deleted. Attaching a file is a user action; keeping the storage behind it consistent is not.
  ],
  purpose: [
    A résumé attached today and an application deleted next month leaves an orphaned file unless
    something is responsible for cleaning it up. That something needs to exist before PL-018
    ships, not after the first orphan is found.
  ],
  description: [
    The file service provides support for storing the event log, resumes, cover letters, etc. for the application.
    The underlying mechanism is a Zip file containing one folder per application, plus a folder called pipeline for e.g.
    the event log.

    The application folders are named after the ID of the event they are associated with. Inasmuch as possible, the
    original file names must be preserved.

    Each application has one slot for a resume and cover letter each. Additionally, a variable number of files maybe
    associated with an application, and managed separately (list field per app, separate UI for navigating those).

    The attachment or detachment of a file to an application is a distinct event, and the event records whether we are speaking
    of a resume, cover letter, or miscellanous file. Detachment means removal from the zip archive. Deletion of an application leaves
    the archive alone, unless the archive is compacted (if such a feature appears).

    Shipped scope: the archive, the `ResumeAttached`/`CoverLetterAttached`/`FileAttached`/
    `AttachmentRemoved` events, and a Dev Tools viewer for what's actually in it (plus a test-file
    button, for exercising the archive independent of PL-018's own attach paths). The `pipeline/`
    folder — a copy of the event log for backup — isn't populated by anything yet; the spec's own
    "e.g." always read as illustrative rather than a concrete requirement. Real on JVM and
    Android; iOS/js/wasmJs get a stub that no-ops, same rollout shape as PL-014's
    `DataPortController`.

    The three event types, not one parameterized by kind — `ResumeAttached`/`CoverLetterAttached`
    read on their own in the event log the way `StatusChanged` does, without opening
    `ApplicationEvent.kt` to check what a `kind` field's possible values are.
  ],
  implementation: (
    "shared/src/commonMain/kotlin/org/cr/pipeline/model/AttachmentKind.kt — RESUME/COVER_LETTER/MISC, the read-side kind on AttachmentRecord",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/AttachmentId.kt, ApplicationEvent.kt — ResumeAttached/CoverLetterAttached/FileAttached/AttachmentRemoved",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationEventReducer.kt — ResumeAttached/CoverLetterAttached each replace the prior one of that kind, FileAttached just appends",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/io/FileArchiveService.kt — the archive interface, entry path convention, UnsupportedFileArchiveService",
    "shared/src/jvmAndroidMain/kotlin/org/cr/pipeline/data/io/JavaZipFileArchiveService.kt — java.util.zip-backed, shared by JVM and Android (new jvmAndroidMain source set in shared/build.gradle.kts)",
    "shared/src/jvmMain, androidMain/kotlin/org/cr/pipeline/data/io/FileArchiveService.*.kt — ~/.pipeline/pipeline-files.zip (JVM), filesDir/pipeline-files.zip (Android)",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/EventSourcedJobApplicationRepository.kt, JobApplicationRepository.kt — attachResume/attachCoverLetter/attachFile/removeAttachment",
    "shared/src/commonMain/kotlin/org/cr/pipeline/di/FileArchiveModule.kt",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/tablet/DevToolsContent.kt — the Files section and its test-file button",
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — attach_resume/attach_cover_letter/attach_file (PL-018)",
  ),
  related: (
    ("PL-018", [Document Attachments (Resume & Cover Letter)]),
    ("PL-020", [File Picker Module]),
  ),
)
