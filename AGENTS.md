# Agent instructions for Pipeline

## Dependency injection

Use Koin for dependency injection. Cross-cutting or platform-backed dependencies get bound in a
Koin module (`org.cr.pipeline.di`) and obtained via `get()`/`koinInject<T>()` — not a hand-rolled
singleton or service locator. See `docs/kotlin-best-practices.md` for the specific patterns
(when a binding needs per-platform modules vs. a single commonMain module with an expect factory).

## Committing

Do not commit changes on your own initiative. Implement and verify the work, then stop and wait
for the change to be reviewed. Only run `git commit` when explicitly asked to.

## Kotlin style and architecture

Follow [docs/kotlin-best-practices.md](docs/kotlin-best-practices.md) for this project's
conventions: the platform-capability (expect/actual) pattern, how to verify a third-party
library's real API before writing code against it, testing conventions, and commit style.

## Documenting the product and its features

Every feature — shipped or planned — gets an entry in the Typst-built catalog under
`docs/product/`. Three docs cover this, from the top down:

- [docs/defining-the-product.md](docs/defining-the-product.md) — the mission statement, scope
  discipline, and how the product doc itself (not a single feature) should be structured and
  governed.
- [docs/writing-features.md](docs/writing-features.md) — how to write a good individual entry:
  summary vs. purpose, status honesty, what belongs in `implementation` and `related`.
- [docs/feature-catalog.md](docs/feature-catalog.md) — the file structure, designator numbering,
  and build commands.
