#import "../template.typ": feature

#feature(
  designator: "PL-018",
  name: "Document Attachments (Resume & Cover Letter)",
  status: "Shipped",
  release: "MVP",
  summary: [
    A resume and a cover letter, attached per application. Uploaded from disk, or picked through
    the file picker. An MCP route offers an agent the same capability — not a raw file upload, but
    whatever shape the protocol treats as first-class: a path, a resource reference.
  ],
  purpose: [
    Which résumé went to which company stops being a mental note, or a folder named
    "resume-v3-final-actually," and becomes part of the record — where it belonged from the start.
  ],
  description: [
    The MCP door: `attach_resume`, `attach_cover_letter`, and `attach_file` each take an `id` and
    a `path` — a location on the same device the MCP server runs on, per the summary's "whatever
    shape the protocol treats as first-class" — read the file at that path, and record the
    attachment through the same `JobApplicationRepository`/event-sourced path PL-031 built.
    `attach_resume`/`attach_cover_letter` each replace whichever one of that kind is already on
    the application; `attach_file` never replaces anything.

    The human-facing door: an "Attachments" section on the Add/Edit form (only once the
    application has an id — a brand-new, unsaved one has nowhere to attach to yet), with a button
    each for résumé, cover letter, and file. Each opens PL-020's file picker and calls the same
    `attachResume`/`attachCoverLetter`/`attachFile` the MCP tools call — one write path underneath
    both doors, same as everywhere else in the app. Existing attachments list underneath, each
    with its own remove button (`removeAttachment` — PL-031 already had the event and the
    repository method; this is its first caller). The section itself degrades to a muted "Not
    available on this platform yet" wherever `FilePicker.isSupported` is false.
  ],
  implementation: (
    "shared/src/jvmMain/kotlin/org/cr/pipeline/data/mcp/McpApp.kt — attach_resume, attach_cover_letter, attach_file; readAttachableFile, filePathArg",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/JobApplicationRepository.kt, EventSourcedJobApplicationRepository.kt — attachResume/attachCoverLetter/attachFile",
    "shared/src/commonMain/kotlin/org/cr/pipeline/sync/event/ApplicationEvent.kt — ResumeAttached/CoverLetterAttached/FileAttached",
    "shared/src/commonMain/kotlin/org/cr/pipeline/ui/screens/AddEditScreen.kt — AttachmentsSection, AttachmentRow",
    "shared/src/commonMain/kotlin/org/cr/pipeline/data/io/FilePicker.kt — the picker interface (PL-020)",
  ),
  related: (
    ("PL-004", [Local Application Database]),
    ("PL-020", [File Picker Module]),
    ("PL-031", [File Management Service]),
  ),
)
