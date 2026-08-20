#import "../template.typ": feature

#feature(
  designator: "PL-027",
  name: "App Password (Biometric Lock)",
  status: "Planned",
  summary: [
    Locks the app behind the platform's own credential store — Face ID, fingerprint, whatever
    Android or iOS already trusts. Pipeline never sees or stores the credential itself.
  ],
  purpose: [
    Job-search data is personal — who you're talking to, what you make now, what you'd take. A
    lock screen that defers entirely to the OS's own biometric store is the honest way to protect
    it: no password of Pipeline's own to leak, because there isn't one.
  ],
  implementation: (),
  related: (),
)
