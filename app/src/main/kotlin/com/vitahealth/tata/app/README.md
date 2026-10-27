# Android application shell (not a bounded context)

This package exists to **assemble** the nine bounded contexts into one app.
It owns Android process entry, a single NavHost, global route/back-stack
coordination and cross-context collaboration, **not feature logic**.

- `di/AppContainer.kt`: shares sessions/HTTP client, delegates factories and
  lifecycle boundaries to `<bc>/infrastructure/di`.
- `integration/medicationoverview`: Treatment catalogue + Intake next dose.
- `integration/pushnavigation`: Omission tap + Monitoring open alert query.
- `navigation/TataNavHost.kt`: root UI navigation and initial destination.
- `navigation/destinations/RootDestination.kt`: existing encoded global routes.
- `navigation/graphs`: **host adapters**, despite feature-related filenames.
  They import app-wide destinations, compose routes across BCs and bind
  callbacks to factories. Do **not** relocate them inside BCs verbatim:
  a BC must not import AppContainer, RootDestination or app/.
- `navigation/deeplink` and `shell`: app-wide deep link and role composition.

A truly self-contained feature registrar belongs in
`<bc>/presentation/navigation` and receives callbacks, not a global app
container. Inventory, Preferences and Analytics already expose examples.

Never instantiate Firebase/Retrofit repositories or clinical domain
policies in this shell. Do not change back-stack/deep-link semantics while
moving source files. See ADR-002 and CODEX-FUNCTIONAL-FIGMA-HANDOFF.
