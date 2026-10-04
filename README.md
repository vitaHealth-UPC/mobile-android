# Tata Android

Native Android application for the Tata product in the VitaHealth project.

## Scope

This repository contains the Android-native product only. The public landing screens belong to `vitaHealth-UPC/landing-page`; mobile scope starts at onboarding and continues through the authenticated adult and caregiver experiences.

## Architecture

The scaffold follows the architecture used in the course materials:

- Bounded Context per Gradle module.
- DDD layers inside every Bounded Context: `domain`, `application`, `infrastructure`, `presentation`.
- CQRS in the application layer using Commands/CommandHandlers and Queries/QueryHandlers.
- Jetpack Compose + StateFlow in presentation.
- Room/SQLite, Retrofit, Coil, WorkManager and Android device APIs in infrastructure as features are implemented.
- No direct dependency from one Bounded Context to another.
- `:app` is the composition root and may depend on every context.
- `:shared` contains only genuinely shared technical/design primitives and never shared business models.

## Modules

| Module | Canonical Bounded Context |
| --- | --- |
| `:identity` | Identity & Subscription |
| `:carelink` | Care Link |
| `:treatment` | Treatment Management |
| `:intake` | Intake Execution |
| `:omission` | Omission & Escalation |
| `:monitoring` | Family Monitoring |
| `:analytics` | Adherence Analytics |
| `:inventory` | Inventory & Replenishment |
| `:preferences` | Accessibility & Preferences |

Support modules:

- `:app`: navigation, composition and cross-context event routing.
- `:shared`: design system, common result types, technical event contracts and synchronization abstractions.

## Baseline

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- Kotlin 2.2.10
- Compose BOM 2026.02.01
- compileSdk / targetSdk 37
- minSdk 29
- JDK 17

## Build

```bash
./gradlew :app:assembleDebug
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat test
```

## Git flow

```text
main
  └── develop
        └── feature/us-xx-short-description
```

Frames in Figma are UX states, not branch names. Use User Stories for feature/fix branches.

See `docs/architecture/` and `docs/ux/figma-frame-map.md`.
