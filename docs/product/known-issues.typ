#import "issues.typ": issues, issue-entry

#heading(level: 1, numbering: none)[Known Issues]

Rough edges in what's already shipped, kept in one place instead of scattered across the entries
that own them. Each feature page carries a short linked pointer back here; the tag on each entry
below links the other way, back to the feature it belongs to.

#for it in issues [
  #issue-entry(it)
]
