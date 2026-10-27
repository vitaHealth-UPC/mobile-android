# Care Link & Consent — Android

This bounded context remains in the single Gradle `:app` module. Its four
layers follow the course's DDD/CQRS criteria, with the backend authoritative
for consent transitions, link lifecycles, code expiry and authorization.

## Structure

```text
carelink/
  domain/
    model/entities/                CareLink, OlderAdultProfile
    model/valueobjects/            CareLinkStatus
    repositories/                  CareLinkRepository
  application/
    commands/                      accept link, consent, adult registration, code generation
    queries/                       adult profile and caregiver profile list
    inputs/                        NewAdultProfile
    ports/                         CaregiverProfilesRepository
    readmodels/                    LinkedAdult, ProfileLinkingCode
  infrastructure/
    remote/                        Retrofit services, DTOs, mappers, adapters
    security/                      SessionScopedCaregiverProfilesRepository
    di/                            CareLinkDependencies
  presentation/
    link/
      CareLinkRoute.kt
      screens/                     CareLinkScreen
      viewmodel/                   CareLinkViewModel, UiState and Step
      ui/                          link cards, progress and button components
      mappers/                     status and stable failure-code UI mapping
    profiles/
      CaregiverProfilesRoute.kt
      screens/                     CaregiverProfilesScreen
      viewmodel/                   CaregiverProfilesViewModel and State
```

## Architecture

- Domain entities protect nonblank identities, including on Kotlin `copy`;
  consent policy and transactional confirmations belong to backend.
- Explicit commands and queries dispatch via domain/application ports.
  The profiles repository returns read models and stays in application ports.
- Infrastructure is responsible for Retrofit, session-ownership checking,
  response validation, and dependency composition.
- Route owns lifecycle/state collection, Screen renders the existing Compose.
  UI components and mappings are extracted without changing visual designs.
  Profiles has no artificial `ui/` or `mappers/`: its code does not require them.
- The application host owns cross-flow route registration in
  `app/navigation/CareLinkNavigationGraph.kt` and supplies `AppContainer`.
  No duplicated `presentation/navigation` is necessary.

## Acceptance limits

JVM regression tests, Android-test Kotlin compilation and architecture tests
are separate from runtime end-to-end acceptance. Linking, consent, code
expiration, caregiver switching, remembered adult profile and PIN setup still
need an integration/device pass. Pending Figma variants are also separate:
this refactor did not redraw or pixel-compare the screens.

No new Gradle submodule, fabricated aggregates/events/offline sync folders,
or Git commit was created. The active plan remains under
`C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
