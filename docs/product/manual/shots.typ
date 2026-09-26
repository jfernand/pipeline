// =============================================================================
// shots.typ — the user manual's screenshot registry.
//
// One entry per screenshot the manual shows. Each entry renders as a labelled
// placeholder until its PNG exists, at which point it renders as the image:
//
//   1. Capture the shot described by its entry below.
//   2. Save it as docs/product/manual/screenshots/<ID>.png  (e.g. SS-01.png).
//   3. Add the ID to `available` below and recompile.
//
// Typst can't test whether a file exists, so `available` is the switch — an ID
// listed there with no PNG behind it is a compile error, not a silent blank.
//
// The same registry renders the manual's "Screenshot Schedule" appendix, so the
// capture list and the placeholders can never disagree about what's wanted.
// =============================================================================

#import "../isss-doc.typ": *

/// IDs that have a real PNG in screenshots/. Add each one as it lands.
#let available = ()

// kind: "window"  — a full desktop window at its default 1280 × 800.
//       "phone"   — a full portrait phone screen (status bar may be cropped off).
//       "crop"    — a tight crop of one region of the desktop window; `height`
//                   is only the placeholder's height, the real image sets its own.
#let shots = (
  "SS-01": (platform: "Desktop", kind: "window",
    subject: [The applications list, fake data on, nothing selected.],
    setup: [Default 1280 × 800 window. Developer mode on, Dev Tools → Show fake data on, app
      restarted. Scroll to the top so both "Needs follow-up" and "All applications" show.]),
  "SS-02": (platform: "Phone", kind: "phone",
    subject: [The applications list, fake data on.],
    setup: [Same fake data as SS-01. Scrolled to the top, no sheet open, the + button visible.]),
  "SS-03": (platform: "Phone", kind: "phone",
    subject: [First launch: the empty list.],
    setup: [Fresh install (or fake data off with no real applications). Nothing typed in search.]),
  "SS-04": (platform: "Desktop", kind: "crop", height: 120pt,
    subject: [Search and filter chips with one status required and one excluded.],
    setup: [Crop the list header (title, search field, chips). Type "eng" in search. Tap
      Interviewing once (amber, required) and Rejected twice (red, excluded).]),
  "SS-05": (platform: "Phone", kind: "phone",
    subject: [The New application form, filled in.],
    setup: [From the list, tap +. Fill Company, Role, pick a status and a source, set Next action.
      Capture the top of the form.]),
  "SS-06": (platform: "Desktop", kind: "crop", height: 150pt,
    subject: [The Attachments section of the Edit form with files attached.],
    setup: [Edit an existing application, attach one résumé and one cover letter, scroll down.
      Crop from the "Attachments" label to the Cancel / Save changes buttons.]),
  "SS-07": (platform: "Desktop", kind: "window",
    subject: [The calendar picker open on the Next action field.],
    setup: [Edit form, click the Next action field so the date picker dialog is showing.]),
  "SS-08": (platform: "Desktop", kind: "window",
    subject: [A full application detail screen.],
    setup: [Pick an application with three or more status-history entries, at least one contact,
      notes, a posting URL, and an overdue reminder (the fake data has one) so the banner shows.]),
  "SS-09": (platform: "Phone", kind: "phone",
    subject: [An application detail screen, top of page.],
    setup: [Same application as SS-08 if possible. Header, Update status button and the start of
      Status history in view.]),
  "SS-10": (platform: "Phone", kind: "phone",
    subject: [The Update status sheet.],
    setup: [From a detail screen tap Update status. Select Interviewing and type a short note
      (e.g. "Panel booked for Thursday").]),
  "SS-11": (platform: "Desktop", kind: "window",
    subject: [The Add contact sheet, filled in.],
    setup: [Detail screen, + beside Contacts. Fill Name, Role and Email; don't save yet.]),
  "SS-12": (platform: "Desktop", kind: "crop", height: 110pt,
    subject: [The Notes field: resting corners, and the same field while editing.],
    setup: [Two crops of the Notes section side by side, or one above the other: first with the
      pointer away (steel corner dots), then after clicking into it (full amber box and ✕).]),
  "SS-13": (platform: "Desktop", kind: "crop", height: 130pt,
    subject: [Status history showing both a person icon and a robot icon.],
    setup: [Change one application's status by hand, then change it again through the MCP
      `edit_application` tool (see chapter 8). Crop the Status history section.]),
  "SS-14": (platform: "Phone", kind: "phone",
    subject: [Swipe-to-delete, mid-swipe.],
    setup: [On the list, drag a card about halfway to the left and hold it so the red trash
      background is visible. Use a throwaway application — releasing all the way deletes it.]),
  "SS-15": (platform: "Desktop", kind: "window",
    subject: [The Delete application? confirmation sheet.],
    setup: [Detail screen, click the trash icon in the header. Don't confirm.]),
  "SS-16": (platform: "Desktop", kind: "window",
    subject: [The Follow-ups screen with overdue items.],
    setup: [Nav rail → Follow-ups, with at least two overdue items (fake data has them).]),
  "SS-17": (platform: "Desktop", kind: "window",
    subject: [Settings, with developer mode and the MCP server both on.],
    setup: [Nav rail → Settings. Developer mode ON, MCP server ON and reading
      127.0.0.1:34687/mcp. Scroll so Data, Developer and MCP server sections are in view.]),
  "SS-18": (platform: "Phone", kind: "phone",
    subject: [Settings on the phone.],
    setup: [List → gear icon. Developer mode on so the Device ID and Event log rows show.]),
  "SS-19": (platform: "Desktop", kind: "crop", height: 80pt,
    subject: [The Data section after a successful export.],
    setup: [Settings → Export as JSON, save the file. Crop the Data section showing
      "Exported N applications" under the row.]),
  "SS-20": (platform: "Desktop", kind: "window",
    subject: [Dev Tools: sandbox switch, device identity and event log.],
    setup: [Developer mode on, nav rail → Dev Tools, with a handful of events in the log.]),
  "SS-21": (platform: "Desktop", kind: "window",
    subject: [The Sync screen (preview).],
    setup: [Nav rail → Sync. Shown as-is — it's placeholder content, the manual says so.]),
  "SS-22": (platform: "Desktop", kind: "phone",
    subject: [The desktop window narrowed below 600 px, showing the phone layout.],
    setup: [Drag the desktop window narrower than 600 px so the nav rail disappears and the
      phone layout takes over. Capture the whole window.]),
)

// The deep link that lands on each shot's screen (chapter 9's route table), so a
// shot can be set up by opening a link instead of clicking through the app.
// `shipped-links` names the ones that work today; every other link is Planned
// (PL-041). `none`
// marks a shot whose state no link can reach — a gesture, a window size, an
// in-progress edit — and still needs the manual steps in its setup.
#let shipped-links = ("pipeline://app/{id}", "pipeline://list")

#let shot-links = (
  "SS-01": "pipeline://list",
  "SS-02": "pipeline://list",
  "SS-03": none,
  "SS-04": "pipeline://list?q=eng&status=interviewing&exclude=rejected",
  "SS-05": "pipeline://app/new",
  "SS-06": "pipeline://app/{id}/edit",
  "SS-07": "pipeline://app/{id}/edit",
  "SS-08": "pipeline://app/{id}",
  "SS-09": "pipeline://app/{id}",
  "SS-10": "pipeline://app/{id}/status",
  "SS-11": "pipeline://app/{id}/contacts/new",
  "SS-12": "pipeline://app/{id}",
  "SS-13": "pipeline://app/{id}",
  "SS-14": "pipeline://list",
  "SS-15": "pipeline://app/{id}/delete",
  "SS-16": "pipeline://followups",
  "SS-17": "pipeline://settings",
  "SS-18": "pipeline://settings",
  "SS-19": "pipeline://settings",
  "SS-20": "pipeline://devtools",
  "SS-21": "pipeline://sync",
  "SS-22": "pipeline://list",
)

#let _fig = counter("manual-figure")

#let _placeholder(id, s, w, h) = box(
  width: w, height: h, fill: panel-sunk, stroke: rule-w + hairline,
  align(center + horizon, block(width: 86%)[
    #set par(justify: false)
    #label(id, color: ink, size: 9pt)
    #v(4pt)
    #label(s.platform + if s.kind != "phone" { " · " + s.kind }, size: 6.5pt)
    #v(9pt)
    #text(font: body-font, size: 8pt, fill: ink-muted, s.subject)
  ]),
)

/// One screenshot with a numbered caption. Placeholder until `id` is in `available`.
#let shot(id, caption) = {
  let s = shots.at(id)
  let (w, h) = if s.kind == "phone" {
    (140pt, 303pt)
  } else if s.kind == "window" {
    (100%, 218pt)
  } else {
    (100%, s.at("height", default: 120pt))
  }
  _fig.step()
  block(breakable: false, above: 16pt, width: 100%)[
    #if id in available {
      box(width: w, stroke: rule-w + hairline, image("screenshots/" + id + ".png", width: 100%))
    } else {
      _placeholder(id, s, w, h)
    }
    #context figure-caption(_fig.display(), caption)
  ]
}

/// Two phone screenshots side by side.
#let shot-pair(a, b) = grid(columns: (1fr, 1fr), column-gutter: 14pt, a, b)

/// The capture list, rendered as a table — the manual's last appendix.
#let screenshot-schedule() = data-table(
  columns: (auto, auto, 1fr),
  header: ("ID", "Platform", "What to capture"),
  ..shots.pairs().map(((id, s)) => (
    text(font: mono-font, size: 8.5pt)[#id#if id in available [ ✓]],
    [#s.platform #linebreak() #text(size: 7.5pt, fill: ink-faint)[#s.kind]],
    [*#s.subject* #linebreak() #text(fill: ink-muted)[#s.setup] #linebreak()
      #text(font: mono-font, size: 7.5pt, fill: ink-faint)[#{
        let l = shot-links.at(id)
        if l == none { "Link: none — manual steps only" }
        else if l in shipped-links { "Link: " + l }
        else { "Link: " + l + " (planned)" }
      }]],
  )).flatten(),
)
