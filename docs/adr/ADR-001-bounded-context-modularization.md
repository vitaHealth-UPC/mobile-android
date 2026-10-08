# ADR-001: Bounded Context package organization

## Status

Accepted.

## Decision

The Android build contains one Gradle module, `:app`. The nine Bounded Contexts are packages under `app/src/main/kotlin/com/vitahealth/tata`, with four layers in each context:

```text
presentation -> application -> domain
infrastructure -------------> domain
```

Application uses CQRS Commands/CommandHandlers and Queries/QueryHandlers. The `app` package composes navigation, factories and cross-context wiring. The `shared` package contains reusable technical and design primitives without business aggregates.

Resources belong to `app/src/main/res`. JVM tests belong to `app/src/test`; device and Compose instrumentation tests belong to `app/src/androidTest`.

## Boundaries

- Domain contains pure Kotlin business rules.
- Context packages use their own contracts and the shared package.
- Cross-context composition belongs to the app package.
- Infrastructure implements networking and storage adapters.
- ViewModels manage presentation state; Compose renders it using native UI elements.
- Figma variants are states of screens when they share the same flow.

The unit architecture checks enforce package isolation and domain independence. Unit tests, APK compilation and device tests verify behavior and Android integration.
