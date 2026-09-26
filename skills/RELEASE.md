---
name: release
description: Cuts a real release of Pipeline (desktop, and Android if changed) — bumps the version via a git tag, builds, installs to the local machine, verifies, and refreshes ~/julio with the final artifacts. Use when the user runs "/release major" or "/release minor", or otherwise explicitly asks to cut/ship a major or minor release. Do NOT use for ad-hoc "rebuild and reinstall" requests that aren't asking for an actual version bump — that's a plain build, not a release.
---

# Release

Cuts a real, tagged release of Pipeline. Takes exactly one argument: `major` or `minor`. If
neither is given, ask which one before doing anything — never guess.

This project's versioning is real semver: a git tag `vX.Y.Z` names release `X.Y.Z` outright,
standing exactly on that commit. Commits after it read `X.Y.Z-N` (N = commits since tag) via
`desktopApp/build.gradle.kts`'s `gitPackageVersion`. Cutting a release is nothing more than
creating the right tag — never hand-edit version numbers anywhere.

## Steps

1. **Determine the bump.** Run `git describe --tags --long --match "v[0-9]*.[0-9]*.[0-9]*"` to
   find the current release `vX.Y.Z`. For `major`, the new tag is `v(X+1).0.0`. For `minor`, it's
   `vX.(Y+1).0`. Never bump the patch digit here — patch releases aren't this skill's job unless
   the user asks for one explicitly (extend by analogy if they do).

2. **Commit outstanding work, if any.** Run `git status` and `git diff`. If there's uncommitted
   work, the user invoking `/release` is their go-ahead to commit it as part of the cycle — draft
   a proper commit message from the actual diff (don't invent one), following the repo's commit
   style and AGENTS.md. If nothing's outstanding, skip this step; don't fabricate an empty commit.

3. **Tag and push.** `git tag -a vX.Y.Z -m "..."` on HEAD, then push both the commit (if any) and
   the tag: `git push origin master && git push origin vX.Y.Z`.

4. **Build.** `./gradlew :desktopApp:packageReleaseDeb`. Verify the output `.deb` filename
   actually reads the expected new version — if it doesn't, stop and investigate before going any
   further (a version mismatch here means the tag didn't take or the build read a stale cache).
   Only run the Android build (`:androidApp:...`) if Android-side files changed since the last
   release — check with `git log`/`git diff` against the previous tag, don't rebuild it by default.

5. **Install locally — ask first.** Installing over the production `.deb`
   (`sudo dpkg -i .../org.cr.pipeline_X.Y.Z_amd64.deb`) needs explicit permission *this turn*,
   even though `/release` was explicitly invoked — confirm with the user before running it. Kill
   any already-running instance first (`pkill -f "/opt/org.cr.pipeline/bin/org.cr.pipeline"`) so
   the reinstall isn't confused by a stale process holding the MCP port.

6. **Verify the installed build actually runs.** Launch it, then confirm:
   - Koin logs "Started N definitions" with no errors.
   - The MCP server binds and answers: find its port via
     `ss -tlnp | grep org.cr.pipeline`, then `curl -X POST http://127.0.0.1:<port>/mcp` returns
     `200`.
   Don't just trust a clean launch log — the MCP bind is the one that's actually failed silently
   before (e.g. a leftover instance from an earlier terminal session holding the port, or a
   double-launch racing itself).

7. **Package final installers.** `./gradlew :desktopApp:copyFinalInstaller` (+
   `:androidApp:copyFinalInstaller` only if Android was rebuilt in step 4). This drops the final
   `.deb`/`.jar`(/`.apk`) into `release-artifacts/`.

8. **Recompile the product docs PDF.** `~/.cargo/bin/typst compile docs/product/pipeline-features.typ`
   — clean exit with no output means success, per this repo's convention. (If the release includes
   product-facing changes that haven't yet gotten a changelog row/revision bump in
   `pipeline-features.typ`, that should already be done as part of the feature work itself, per
   `docs/feature-catalog.md` — this skill doesn't add one on its own initiative.)

9. **Refresh `release-artifacts/` and `~/julio`.** Delete stale versioned files (the previous
   release's `.deb`/`.jar`/apks) from both `release-artifacts/` and `~/julio`, then copy the fresh
   ones plus the recompiled `pipeline-features.pdf` into `~/julio`. Leave anything in `~/julio`
   that isn't a Pipeline release artifact (other files the user keeps there) untouched.

10. **Report back**: the new version, what changed since the last release (from
    `git log <prev-tag>..HEAD --oneline`), and confirmation that the installed instance verified
    clean.

## Constraints carried over from AGENTS.md and this session's standing rules

- Never `git commit` except as this cycle's own step 2, driven by the user's `/release`
  invocation itself — not proactively beyond that.
- Never `sudo dpkg -i` without explicit confirmation in the same turn, even mid-skill.
- Never force-push, never skip hooks.
