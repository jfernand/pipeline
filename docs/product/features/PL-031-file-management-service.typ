#import "../template.typ": feature

#feature(
  designator: "PL-031",
  name: "File Management Service",
  status: "Planned",
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
  ],
  implementation: (),
  related: (
    ("PL-018", [Document Attachments (Resume & Cover Letter)]),
    ("PL-020", [File Picker Module]),
  ),
)
