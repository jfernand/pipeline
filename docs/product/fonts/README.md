# Fonts

Barlow Condensed (600/700/800), IBM Plex Mono (400/500/600), Space Grotesk (400/500/700) —
the exact families and weights `ISSS Document Design.dc.html` links from Google Fonts. Pulled
directly from `fonts.gstatic.com` as static TTFs (Typst in this environment doesn't load WOFF2).
All three are SIL Open Font License.

Compile with:

```
typst compile --font-path fonts pipeline-features.typ
```
