#import "../template.typ": feature

#feature(
  designator: "PL-018",
  name: "Document Attachments (Resume & Cover Letter)",
  status: "Planned",
  summary: [
    A resume and a cover letter, attached per application. Uploaded from disk, or picked through
    the file picker. An MCP route offers an agent the same capability — not a raw file upload, but
    whatever shape the protocol treats as first-class: a path, a resource reference.
  ],
  purpose: [
    Which résumé went to which company stops being a mental note, or a folder named
    "resume-v3-final-actually," and becomes part of the record — where it belonged from the start.
  ],
  implementation: (),
  related: (("PL-004", [Local Application Database]), ("PL-020", [File Picker Module])),
)
