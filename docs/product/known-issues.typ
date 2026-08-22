#import "isss-doc.typ": ink-faint
#import "issues.typ": issues, fixed-issues, issue-entry

#heading(level: 1, numbering: none)[Known Issues]

Rough edges in what's already shipped, kept in one place instead of scattered across the entries
that own them. Each feature page carries a short linked pointer back here; the tag on each entry
below links the other way, back to the feature it belongs to.

#for it in issues [
  #issue-entry(it)
]

#heading(level: 2, numbering: none)[Fixed]

Resolved issues, moved here verbatim rather than deleted — what the issue actually said, not a
summary of the fix. See the catalog's Revision History for when and what changed.

#for it in fixed-issues [
  #text(fill: ink-faint, issue-entry(it))
]
