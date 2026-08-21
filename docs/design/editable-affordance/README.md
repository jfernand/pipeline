# Editable Affordance   

Open `Editable Affordance.dc.html` in a browser to view it (needs network access for Google Fonts
and the React/ReactDOM/Babel CDN scripts `support.js` loads).

Seven treatments for signaling that a run of text or a field value is editable, without a
persistent input-field border:

- **1a** Corner dots
- **1b** Corner chevrons
- **1c** Edge brackets
- **1d** Baseline ticks
- **1e** Dots, resting — steel dots at rest, amber chevrons on hover. The pick.
- **1f** State ladder for 1e — rest / hover / editing / empty / rejected / locked
- **1g** Hard cases — line wrap, adjacent fields (fails), display type, table cell

PL-032 (`docs/product/features/PL-032-editability-affordance-language.typ`) tracks this as a
planned feature. The Compose implementation of 1e/1f lives in
`shared/src/commonMain/kotlin/org/cr/pipeline/ui/components/EditableAffordance.kt`.
