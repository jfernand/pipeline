# Kotlin best practices for Pipeline

These are the patterns this codebase actually follows, written down so they stay consistent as
more of it gets built. If you're changing `shared/`, read this first.

## Dependency injection: Koin, always

Every cross-cutting or platform-backed dependency (repositories, stores, controllers) is bound in
a Koin module and obtained via constructor injection (`get()` inside a module block) or
`koinInject<T>()` in Compose code. Don't reach for a hand-rolled singleton, a top-level `object`
holding mutable state, or service-locator code that bypasses Koin — even for something that only
has one implementation. Koin is what makes it possible to swap a real implementation for a fake
one in tests and previews without touching call sites.

Modules live under `org.cr.pipeline.di`. A module is `expect`/`actual` per platform **only** when
the *binding itself* differs per platform (e.g. `platformDataModule`, which is Room-backed on
android/jvm/ios and in-memory on js/wasmJs). If every platform binds the same interface through
the same expect *factory function*, one plain `module { }` in commonMain calling that factory is
enough — see `mcpDataModule`, which is a single commonMain module wired to the
`expect fun createMcpServerController(...)` factory described below.

## The platform-capability pattern

When a capability needs a different real implementation per platform (or isn't available on some
platforms at all), the shape is always:

1. A `commonMain` interface expressed only in primitive/domain types — never a platform-SDK type
   (no Ktor, no Room, no `android.content.Context`) leaks into the interface.
2. An `expect fun createXxx(...): XxxInterface` factory function in `commonMain`.
3. One `actual` per leaf target that needs a *real* implementation (`androidMain`, `jvmMain`,
   `iosMain`, `jsMain`, `wasmJsMain` — not an intermediate source set like `roomMain`, unless every
   target sharing that source set genuinely wants the same implementation).
4. Targets where the capability can't run for real (no platform API, no library variant, or a
   fundamental constraint like a browser sandbox not being able to open a listen socket) get a
   stub actual: report `isSupported = false` (or equivalent) and no-op, rather than throwing.
   Callers gate on that flag to decide whether to show UI for the capability at all.

See `PreferencesSettingsFactory` (five real actuals, one per platform, because
`multiplatform-settings` has a real implementation everywhere) and `McpServerController` (one real
actual on `jvmMain`, one shared `UnsupportedMcpServerController` stub used by every other actual,
because the MCP server's Ktor/http4k dependencies plus loopback socket binding only make sense on
desktop) as the two concrete examples.

Don't add a platform-specific dependency to `commonMain.dependencies` — put it only in the source
set(s) that actually need it, so unsupported targets never have to resolve a variant that doesn't
exist for them.

## Verify library APIs from real sources, not memory or search summaries

Before writing code against a third-party library — especially a Kotlin Multiplatform one, where
which targets actually publish a variant is easy to get wrong — pull the real artifact and read
it:

- Check the actual Maven Central directory listing for available versions rather than trusting a
  search index (search indexes have produced false "not found" results in this project before).
- For KMP libraries, inspect the Gradle module metadata (`.module`) to see which target variants
  a specific sub-artifact actually publishes — a library's top-level README can describe the
  umbrella package while a specific capability (e.g. a server module) only ships for a subset of
  targets.
- Download the `-sources.jar` and read the real class/function signatures before calling them.
  A one-line assumption about a type ("this numeric arg will deserialize as a `Long`") can be
  wrong in a way that only shows up at runtime — verify it in the source instead of guessing.

This is slower than trusting a summary, but it's what caught real problems in this codebase
(a wrong `kotlinx-browser` version pulled from a doc summary; the MCP JSON layer normalizing any
in-`Int`-range `Long` down to a 32-bit representation, which would silently break a naive
`Tool.Arg.long()` application-id lens).

## Prefer a real library over hand-rolling

If a well-maintained library already solves the problem (key-value storage, an MCP server, HTTP
routing), use it — don't hand-roll a parallel implementation "to keep things simple." This
project switched from a hand-rolled preferences store to `multiplatform-settings`, and from a
hand-rolled JSON-RPC layer to the MCP SDK precisely because of this. The cost of a new dependency
is real, but it's usually smaller than the cost of maintaining a bespoke reimplementation of
something a library already gets right.

## Testing

- Plain `kotlin.test` (`@Test`, `assertEquals`, `assertIs`, ...) with `kotlinx.coroutines.test.runTest`
  for anything suspending. No other test framework.
- Test names are backtick-quoted sentences describing behavior
  (`` `saveApplication with no id constructs the application and persists it` ``), not
  `camelCaseMethodNames`.
- In-memory test doubles for interfaces are named `FakeXxx` (e.g. `FakeApplicationStateStore`,
  `InMemoryEventLog`) and live where every test that needs them can reach them: put a fake in
  `commonTest` once more than one test needs it, rather than duplicating a private copy per file.
- Prefer standing up the real production wiring against fakes over mocking framework internals.
  For the MCP server, that means building the actual `HttpHandler` the app serves and driving it
  in-process with http4k's own MCP client (swapping its `HttpHandler` for the in-memory one
  instead of a real `JavaHttpClient`) — no sockets, no real server needed to exercise the whole
  request/response path.

## Avoid premature abstraction

Don't add an abstraction layer, a config flag, or a fallback path for something the app doesn't
need yet. A bug fix doesn't need a refactor riding along with it. Three similar-looking lines
across two small data classes is fine; a shared base class for the sake of avoiding that
duplication usually isn't, until there's a third caller.

## Commits

Small commits that each tell one part of the story (dependencies, then the contract, then the
real implementation, then wiring, then UI), imperative present tense subject lines
("Add X", "Wire Y into Z"), a body only when the *why* isn't obvious from the diff. Match the
existing style in `git log` rather than inventing a new one.

**Changes must be reviewed by a human before they're committed.** Implement and verify (compile,
test, and — for anything with a UI — actually run it), then stop and let the change be reviewed.
Don't `git commit` on your own initiative; wait to be asked.
