#import "../template.typ": feature

#feature(
  designator: "PL-028",
  name: "Calendar Integration",
  status: "Planned",
  release: "1.1",
  summary: [
    Reads and writes the platform calendar. An interview gets a reminder there, not just inside
    Pipeline.
  ],
  purpose: [
    A reminder that only fires inside an app the user has to remember to open is a reminder that
    gets missed. The calendar is where people already look.
  ],
  implementation: (),
  related: (("PL-029", [Interview Events & Notes]),),
)
