#import "../template.typ": feature

#feature(
  designator: "PL-031",
  name: "File Management Service",
  status: "Planned",
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
  implementation: (),
  related: (
    ("PL-018", [Document Attachments (Resume & Cover Letter)]),
    ("PL-020", [File Picker Module]),
  ),
)
