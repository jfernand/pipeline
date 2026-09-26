#import "isss-doc.typ": *
#import "manual/shots.typ": shot, shot-pair, screenshot-schedule

#show: isss-doc.with(
  title: "Pipeline",
  subtitle: "Tracking job applications on the desktop and the phone.",
  class: "User Manual",
  doc-id: "ISSS-UM-0001",
  revision: "0.1",
  date: "2026-09-25",
  status: "Draft",
  applies-to: "Pipeline desktop (Linux, macOS, Windows) and Android",
  owner: "Javier Fernández",
  classification: "",
)

#heading(level: 1, numbering: none)[About This Manual]

#heading(level: 2, numbering: none)[Who it's for]

This manual is for people who use Pipeline, not people who build it. It says what each screen is
for, what every control does, and — where it matters — what doesn't work yet. The feature catalog
(ISSS-0001) covers the same ground from the other side: where the code is and why it's shaped the
way it is.

#note[Feature designators like PL-024 appear in the margin now and then. They're the catalog's
  permanent names for features — useful when reporting a problem, safe to ignore otherwise.]

#heading(level: 2, numbering: none)[Desktop and phone]

Pipeline runs on the desktop (Linux, macOS, Windows) and on Android phones. It is one app with two
layouts, and the width of the window picks between them — nothing else:

- *Desktop layout* — wider than 600 points. A nav rail down the left edge; the list lays out in a
  grid of cards.
- *Phone layout* — narrower than 600 points. One screen at a time, list to detail and back, with a
  gear icon for Settings.

A phone always gets the phone layout. A desktop window gets the desktop layout at its usual size,
and the phone layout if you drag it narrow. Where this manual says "on the desktop" or "on the
phone" it means the layout, not the device — except where a feature needs the desktop itself
(attachments, export and import, the MCP server). Those are called out every time.

#heading(level: 2, numbering: none)[Conventions]

Button and field names are set in *bold*, exactly as they read on screen. Keys are written
`Return`, `Esc`, `Shift+Return`. Screenshots carry an ID (SS-01, SS-02, …); in this draft, any not
yet captured show as a grey panel naming what goes there.

#heading(level: 2, numbering: none)[Revision History]

#data-table(
  columns: (auto, auto, 1fr),
  header: ("Rev", "Date", "Changes"),
  [0.1], [2026-09-25], [First draft. Covers everything shipped through PL-042. Screenshots are
    placeholders pending capture — see Appendix C.],
)

// =============================================================================
= Getting Started

== What Pipeline is

Pipeline tracks job applications. For each one: the company, the role, where it stands, when you
applied, when you next need to do something, who you've talked to, what you sent them, and what
you want to remember. Nothing else, and nothing less.

It keeps all of that on your own device. There's no account, no server, and no telemetry. On the
desktop it can also run a small MCP server, so an AI agent you run yourself can read and update
the same applications you see on screen (chapter 8).

== Installing

=== Desktop

Pipeline is built as a native installer per platform: a `.deb` for Debian and Ubuntu Linux, a
`.dmg` for macOS and an `.msi` for Windows. Install it the usual way for your system — open the
`.dmg` and drag Pipeline to Applications; double-click the `.msi`; or on Linux:

#transcript[
  #prompt sudo apt install ./org.cr.pipeline\_\*.deb
]

Launch Pipeline from your applications menu afterwards.

=== Android

Pipeline needs Android 7.0 or later. Install the `.apk` you were given — your phone will ask you
to allow installs from the app you opened it with (Files, a browser, your mail app) the first
time. Pipeline then appears in your app drawer with an amber "PL" icon.

== First launch

A fresh install opens on an empty list. Nothing is filled in for you — the list stays empty until
you add your first application (chapter 3).

#note[Want something to click around in first? Chapter 10 describes a sandbox of made-up
  applications you can turn on and off without touching your own.]

#shot("SS-03", [First launch on the phone. The + button bottom-right adds an application.])

Next time you open Pipeline it goes back to where you left off: if you were looking at an
application, it reopens that application; if you were on the list, it opens the list.

// =============================================================================
= The Applications List

The list is home. Every other screen opens from here, and comes back here when it's done.

#shot("SS-01", [The desktop layout. Nav rail on the left, applications as a grid of cards, the
  New button bottom-right.])

#shot-pair(
  shot("SS-02", [The phone layout. Settings is behind the gear icon, top-right.]),
  shot("SS-22", [The same desktop app, narrowed below 600 points: it switches to the phone
    layout.]),
)

== Reading a card

Each application is one card:

#data-table(
  columns: (auto, 1fr),
  header: ("Part", "What it tells you"),
  [Company and role], [The first two lines, in that order.],
  [`12d`], [Top-right: days since you applied. `0d` if there's no applied date.],
  [Status chip], [Where it stands — Wishlist, Applied, Phone Screen, Interviewing, Offer, Rejected
    or Withdrawn. Each status has its own colour, used everywhere in the app.],
  [Overdue badge], [A small tab over the status chip's corner when the next-action date has
    passed, counting the days overdue. The card's border turns amber too.],
  [Paperclip], [The application has at least one attached file.],
  [`Applied 12d ago`], [Bottom-right: how long since you applied. Wishlist entries read
    `Created …` instead, since you haven't applied yet.],
)

== Needs follow-up

The list has two sections. *Needs follow-up* collects every application whose next-action date
has passed, so the ones waiting on you sit at the top. *All applications* holds the rest. An
application moves out of Needs follow-up the moment you give it a later next-action date, or clear
the date.

== Searching

Type in the search field and the list narrows as you type, matching company or role, ignoring
case. Clear the field to see everything again.

== Filtering by status

Under the search field is a row of chips, one per status, plus *All*. Each status chip has three
states, and tapping it steps through them in order:

#data-table(
  columns: (auto, auto, 1fr),
  header: ("State", "Looks like", "Means"),
  [Not set], [Plain], [This status doesn't affect the list.],
  [Required], [Amber], [Show applications with this status. Several required chips mean "any of
    these".],
  [Excluded], [Red], [Hide applications with this status, whatever else is set.],
)

A fourth tap returns the chip to not set. The chips are independent, so any mix works:
Interviewing and Offer required with Withdrawn excluded reads "interviews and offers, never
withdrawn ones". Excluding Rejected alone reads "everything except rejections". Tap *All* to clear
every chip at once.

Search and chips combine: an application has to match both to show.

#shot("SS-04", [Searching for "eng" with Interviewing required (amber) and Rejected excluded
  (red).])

#note[Filters reset when you leave the list. Remembering them is planned (PL-043).]

// =============================================================================
= Adding and Editing

== Adding an application

On the phone, tap the amber *+* button at the bottom-right of the list. On the desktop, click
*New* at the bottom-right. Both open the *New application* form.

#xref("PL-006")[Add Application]

Fill in what you know — only Company and Role really matter to start, and everything can be
changed later. Then tap *Save application* at the bottom, or *Save* at the top. The ✕ at the
top-left, or *Cancel* at the bottom, leaves without saving.

#shot("SS-05", [The New application form on the phone.])

#data-table(
  columns: (auto, 1fr),
  header: ("Field", "Notes"),
  [Company], [The employer's name. Shown large on the card and the detail screen.],
  [Role], [The job title.],
  [Status], [Tap one. New applications start at Applied; pick Wishlist for one you haven't sent
    yet.],
  [Date applied], [Filled in with today's date for a new application. Tap it to pick a different
    day from a calendar; tap its ✕ to clear it.],
  [Next action], [When you next need to do something — chase a reply, send a thank-you. Once this
    date passes, the application moves into Needs follow-up. Optional; tap ✕ to clear.],
  [Posting URL], [Where the job was advertised.],
  [Source], [How you found it: Referral, LinkedIn, Company site, Recruiter or Other.],
  [Notes], [Anything you'll want to remember in three weeks.],
)

#shot("SS-07", [Picking a Next action date from the calendar.])

== The statuses

#data-table(
  columns: (auto, 1fr),
  header: ("Status", "Use it for"),
  [Wishlist], [A job you want to apply for and haven't yet.],
  [Applied], [Sent, waiting to hear.],
  [Phone Screen], [A first call is booked or done.],
  [Interviewing], [You're in the interview rounds.],
  [Offer], [You have an offer.],
  [Rejected], [They said no.],
  [Withdrawn], [You pulled out.],
)

Pipeline doesn't force an order — you can move from any status to any other. Every change is
recorded in the application's status history (chapter 4).

== Editing an application

Open the application, then tap the pencil icon in its header. This opens the same form as *New
application*, titled *Edit application*, with everything filled in. Change what you need and tap
*Save changes*.

#xref("PL-007")[Edit Application]

For the two things you change most — the status and the notes — there are quicker ways that don't
need the form at all: the *Update status* button, and editing notes in place. Both are in the next
chapter.

== Attaching files

On the desktop, each application can hold a résumé, a cover letter and any number of other files, so which version
went to which company is part of the record rather than something to remember.

#xref("PL-018")[Document Attachments]

Attachments live at the bottom of the *Edit application* form — not the New one, since an
application needs saving once before anything can attach to it. Click *Résumé*, *Cover letter* or
*File*, and pick a file from disk. What's attached lists above the buttons; the ✕ at the end of a
row removes that file.

An application holds one résumé and one cover letter at a time: attaching a new résumé replaces the
old one. *File* never replaces anything.

#shot("SS-06", [Attachments on the Edit form: a résumé and a cover letter attached.])

#warning[On Android the Attachments section reads "Not available on this platform yet." Attach
  files from the desktop.]

// =============================================================================
= An Application in Detail

Tap any card to open it. On the desktop the detail fills the content area, with *Applications*
and a back arrow at the top-left to return to the list; on the phone it's its own screen with a
back arrow.

#shot("SS-08", [The desktop detail screen: header and actions across the top, history and contacts
  on the left, notes, reminders and posting on the right.])

== The header

Company, role, current status, and how many days since anything last happened to this
application. Alongside them, the actions:

#data-table(
  columns: (auto, 1fr),
  header: ("Control", "Does"),
  [*Update status*], [Opens the Update status sheet — see below.],
  [Pencil], [Opens the Edit application form.],
  [Trash can], [Deletes the application, after asking (chapter 5).],
)

#shot-pair(
  shot("SS-09", [The phone detail screen. Pencil and trash sit in the top bar.]),
  shot("SS-10", [The Update status sheet.]),
)

== Updating the status

Tap *Update status*. A sheet slides up from the bottom, with the company and role at the top so
you know which application you're changing. Pick the *New status*, add a *Note* if there's
something worth remembering about this step — "Panel booked for Thursday", "Recruiter says budget
is frozen" — and tap *Save update*. The change is dated today.

#xref("PL-005")[Update Application Status]

== Status history

Every status an application has had, newest at the top, each with its date and note. It's
recorded automatically — every status change adds an entry, whether it came from Update status,
the Edit form, or an AI agent.

Beside each date is a small icon saying who made that change:

#data-table(
  columns: (auto, 1fr),
  header: ("Icon", "Means"),
  [Person], [Changed by hand, on one of your devices.],
  [Robot], [Changed by an AI agent through the MCP server (chapter 8).],
)

#xref("PL-034")[Event Provenance Indicator]

#shot("SS-13", [A history mixing both: the robot marks the change an agent made.])

== Contacts

The people you've dealt with at this company. Tap *+* beside the *Contacts* heading to add one:
*Name*, *Role* (recruiter, hiring manager…) and *Email*, then *Save contact*.

#shot("SS-11", [Adding a contact.])

== Notes, edited in place

Notes don't need the Edit form. Click or tap the text under *Notes* — or *Add notes* if there are
none — and type.

#xref("PL-032")[Editability Affordance Language]

Pipeline marks anything you can edit in place the same way: small corner marks around it. They're
quiet steel dots at rest, turn into amber corners when the pointer is over them, and close into a
full amber box while you're typing.

#shot("SS-12", [Notes at rest (corner dots), and while editing (full amber box with ✕).])

#data-table(
  columns: (auto, 1fr),
  header: ("To", "Do"),
  [Save], [`Return`, or click or tap anywhere else. On a phone keyboard, tap *Done*.],
  [Start a new line], [`Shift+Return`. (Not available from a phone's on-screen keyboard, whose
    Return key saves.)],
  [Cancel], [`Esc`, or the ✕ beside the field. Your edit is dropped and the old notes come back.],
)

== Reminders and the overdue banner

When an application has a reminder whose date has passed, an amber *Follow-up overdue* banner
sits above the status history saying what's due and when. On the desktop, all of an application's
reminders are also listed under *Reminders* in the right-hand column, overdue ones in amber.

#note[There is no way to create a reminder from the app yet — the + beside Reminders does
  nothing. The next-action date is the working way to schedule a follow-up today.]

== Posting

If the application has a posting URL, it's shown under *Posting*, with the source and the date
applied.

The posting link and the *Posting* button show the URL but don't open it in a browser yet; copy
it by hand for now.

// =============================================================================
= Deleting an Application

A mis-added entry, a duplicate, an application you'd rather not see: delete it. There are two ways,
and they differ in how much they ask first.

#xref("PL-024")[Delete Application]

*From the detail screen* (desktop and phone): tap the trash can in the header. A sheet asks
*Delete application?* and names the application. Tap *Delete* to confirm, or *Cancel*. Confirming
takes you back to the list.

#shot("SS-15", [The confirmation sheet.])

*From the phone list*: swipe a card to the left. A red background with a trash can appears behind
it. Swipe all the way and the application is deleted — *there is no confirmation step*, because
the swipe itself is deliberate.

#shot("SS-14", [Mid-swipe. Let go before the end and the card springs back.])

#danger-block[There's no undo for a delete in the app. A deleted application disappears from the
  list, search, follow-ups and the MCP tools at once. Its history isn't erased — it stays in
  Pipeline's event log — but nothing in the app brings it back. If you're unsure, change its
  status to Withdrawn instead.]

// =============================================================================
= Follow-ups

#text(font: mono-font, size: 9pt, fill: ink-faint)[DESKTOP LAYOUT]

The *Follow-ups* item in the nav rail answers one question: what needs doing now? It lists every
overdue next-action date and every overdue reminder, across all your applications, most overdue
first. Click a row to open that application.

#xref("PL-010")[Follow-ups]

When nothing's overdue it says so: "Nothing overdue right now."

#shot("SS-16", [Follow-ups with two applications waiting on you.])

On the phone layout there's no Follow-ups screen; the *Needs follow-up* section at the top of the
list covers overdue next-action dates.

// =============================================================================
= Settings

On the desktop, *Settings* is at the bottom of the nav rail. On the phone, it's the gear icon at
the top-right of the list.

#xref("PL-012")[App Preferences]

#shot("SS-17", [Settings on the desktop, with developer mode and the MCP server on.])

#data-table(
  columns: (auto, 1fr),
  header: ("Row", "Does"),
  [*Export as JSON*], [Desktop only. Saves every application to a `.json` file you choose.],
  [*Import from file*], [Desktop only. Reads a Pipeline `.json` export and adds its applications.],
  [*Pipeline*], [Under About: the version you're running.],
  [*Developer mode*], [Shows developer rows in Settings and Dev Tools in the nav rail (chapter
    10). Off by default.],
  [*MCP server*], [Desktop only. Turns the agent server on and off, and sets its port (chapter
    8).],
)

#shot("SS-18", [Settings on the phone, developer mode on.])

== Export and import

On the desktop, *Export as JSON* writes every application — company, role, status, dates, posting, source,
notes — to one file, `pipeline-export-<date>.json` unless you name it otherwise. The file is plain
and unencrypted: it's yours, and it's readable by anything. Keep it somewhere private.

#xref("PL-014")[Export / Import]

*Import from file* reads such a file and adds every application in it as a new entry. It never
changes or replaces what's already there — so importing the same file twice gives you two of
everything.

The result shows under the row: "Exported 12 applications", "Imported 12 applications", or what
went wrong.

#shot("SS-19", [After a successful export.])

#warning[Export carries the application record only. Attached files, status history and contacts
  aren't in the file.]

== Sync and pairing

*Sync isn't working yet.* The Sync screen, the *Pair a device* and *Sync over* rows, the
paired-device card and the QR code are a preview of the design — the devices, counts and log lines
they show are sample content, and nothing leaves your device. Use export and import to move data
between desktops until sync ships.

#shot("SS-21", [The Sync screen, showing preview content.])

The *Follow-up reminders* row is likewise a preview; it doesn't schedule notifications yet.

// =============================================================================
= Using Pipeline with an AI Agent

#text(font: mono-font, size: 9pt, fill: ink-faint)[DESKTOP ONLY]

Pipeline's desktop app can run an MCP server — the Model Context Protocol, the standard way AI
agents such as Claude call out to tools. Turn it on and an agent running on the same computer can
list your applications, add and edit them, attach files, and bring an application up on your
screen. It works on the very same data you see — not a copy.

#xref("PL-013")[MCP Server Infrastructure]

== Turning it on

In Settings, click the *OFF* beside *MCP server* so it reads *ON*. The row then shows the address
it's listening on:

#listing[`http://127.0.0.1:34687/mcp`]

The server only listens on `127.0.0.1` — this computer. Nothing else on your network can reach it.
It runs while Pipeline is open and stops when you close it.

*Changing the port.* 34687 is the default. To use another, click the port number (it has the
editable corner marks), type a new one between 1 and 65535, and press `Return`; `Esc` cancels.
The server restarts on the new port. The port can only be edited while the server is on.

== Connecting an agent

Point your agent at the address above as an HTTP MCP server. With Claude Code, for example:

#transcript[
  #prompt `claude mcp add --transport http pipeline http://127.0.0.1:34687/mcp`
]

Other agents take the same URL in their own MCP settings. Pipeline must be running, with the
server on, whenever the agent wants to use it.

== What an agent can do

#data-table(
  columns: (auto, 1fr),
  header: ("Tool", "Does"),
  [`list_applications`], [Lists every application: id, company, role, status, applied date.],
  [`add_application`], [Adds a new application.],
  [`edit_application`], [Changes an existing application, by id.],
  [`attach_resume`], [Attaches a file on this computer as the application's résumé.],
  [`attach_cover_letter`], [Same, as the cover letter.],
  [`attach_file`], [Attaches any other file.],
  [`open_application`], [Brings that application's detail screen up in the running app.],
  [`list_settings`], [Reports Pipeline's settings, including where the MCP server is listening.],
)

So "add the Rocket Companies application, senior engineer, applied today" or "show me the Acme
one" work without you touching the app. Anything an agent changes carries the robot icon in the
status history (chapter 4), so you can always tell its work from yours.

// =============================================================================
= Links and Picking Up Where You Left Off

Every application has a link of the form

#listing[`pipeline://app/42`]

where 42 is the application's id (`list_applications` reports it). Opening one — from a note, a
calendar entry, a chat — launches Pipeline straight onto that application's detail screen. On
Android this works as soon as Pipeline is installed. On the desktop, Pipeline opens the link when
it's launched with one, but it doesn't yet register itself with the operating system as the
handler for `pipeline://` links — so clicking one elsewhere may do nothing.

#xref("PL-009")[In-App Navigation]

A second link, `pipeline://list`, opens the applications list. Those two are the only links
Pipeline answers today; every other screen is reached by clicking through the app. The table
lists the full set of links Pipeline needs — one for every screen and sheet in this manual — with
the ones that work marked Shipped and the rest Planned.

#xref("PL-041")[Routes and Deep Links]

#{
show raw: set text(size: 7.5pt)
data-table(
  columns: (auto, auto, 1fr),
  header: ("Link", "Status", "Opens"),
  [`pipeline://app/{id}`], [Shipped], [An application's detail screen.],
  [`pipeline://list`], [Shipped], [The applications list, unfiltered.],
  [`pipeline://list?…`], [Planned], [The list with a search and status filters applied:
    `q` for the search text, `status` for required chips, `exclude` for excluded ones, the last
    two comma-separated. SS-04 in Appendix C has a worked example.],
  [`pipeline://app/new`], [Planned], [The New application form.],
  [`pipeline://app/{id}/edit`], [Planned], [The Edit application form.],
  [`pipeline://app/{id}/status`], [Planned], [The detail screen with the Update status sheet
    open.],
  [`pipeline://app/{id}/contacts/new`], [Planned], [The detail screen with the Add contact sheet
    open.],
  [`pipeline://app/{id}/delete`], [Planned], [The detail screen with the delete confirmation
    open. Opening it never deletes anything by itself.],
  [`pipeline://followups`], [Planned], [Follow-ups (desktop layout; the list on the phone).],
  [`pipeline://settings`], [Planned], [Settings.],
  [`pipeline://settings/pair`], [Planned], [Pair a device.],
  [`pipeline://sync`], [Planned], [The Sync screen.],
  [`pipeline://devtools`], [Planned], [Dev Tools, when developer mode is on; Settings
    otherwise.],
)
}

Separately, Pipeline remembers what you were looking at. Close it on an application's detail and
it reopens on that application; close it on the list and it reopens on the list. Settings, the
forms and Dev Tools don't count — you'll land back on whichever list or application you visited
before them. A link always wins over this.

#xref("PL-021")[Remember Last Route]

// =============================================================================
= Dev Tools

Turn on *Developer mode* in Settings and Pipeline shows its workings. On the desktop, *Dev Tools*
appears in the nav rail just above Settings; on the phone, tap the *Event log* row in Settings.

#xref("PL-019")[Fake Data Mode]

#shot("SS-20", [Dev Tools on the desktop.])

== A sandbox of made-up applications

*Show fake data* under *Sandbox* switches Pipeline to a separate set of made-up applications, so
you can try things — delete, filter, swipe — without risking your own. Restart Pipeline for the
switch to take effect, in either direction.

Your real applications are untouched while it's on, and the fake ones are kept separately: turn it
off, restart, and your own data is back exactly as you left it. Anything you did to the fake
applications is still there the next time you turn it on.

#warning[Check which mode you're in before doing real work. Applications added with fake data on
  go into the sandbox, not your real list.]

== Device and event log

*Device* shows this device's id. *Event log* lists every change ever made — every creation, edit,
status change, contact, attachment and delete — as Pipeline records it. This log is the actual
record: everything else you see is rebuilt from it. *Files* lists what's in the attachment
archive.

// =============================================================================
#show: appendices

= Gestures and Keys

#data-table(
  columns: (auto, auto, 1fr),
  header: ("Where", "Gesture or key", "Does"),
  [List], [Tap a card], [Opens the application.],
  [List], [Tap a status chip], [Steps it: not set → required → excluded → not set.],
  [List], [Tap *All*], [Clears every status filter.],
  [Phone list], [Swipe a card left, all the way], [Deletes it — no confirmation.],
  [Notes], [`Return` / *Done*], [Saves.],
  [Notes], [`Shift+Return`], [New line (hardware keyboard only).],
  [Notes], [`Esc` / ✕], [Cancels the edit.],
  [MCP port], [`Return`], [Saves the new port.],
  [MCP port], [`Esc`], [Cancels.],
  [Date fields], [Tap], [Opens the calendar picker.],
  [Date fields], [✕], [Clears the date.],
)

= Where Your Data Lives

Everything Pipeline stores stays on the device it's on.

#data-table(
  columns: (auto, 1fr),
  header: ("Platform", "Location"),
  [Desktop], [The folder `.pipeline` in your home folder: `pipeline.db` holds the event log,
    `pipeline-files.zip` holds attachments.],
  [Android], [Pipeline's private app storage, which other apps can't read. Android's own device
    backup may include it.],
)

To back up the desktop app, close Pipeline and copy the whole `.pipeline` folder — that includes
attachments and full history, which an export doesn't. Restore by putting the folder back before
starting Pipeline.

#warning[Android has no export yet. Uninstalling Pipeline deletes its data from the phone; only
  Android's own device backup, if you use it, may keep a copy.]

= Screenshot Schedule

The screenshots this manual needs, in the order they appear. A ✓ after an ID means it's been
captured. To add one: save it as `docs/product/manual/screenshots/<ID>.png`, add the ID to
`available` in `docs/product/manual/shots.typ`, and recompile.

Desktop shots are taken at the default 1280 × 800 window unless the entry says otherwise. Most use
the fake-data sandbox (chapter 10), so no real applications appear in the manual.

#screenshot-schedule()
