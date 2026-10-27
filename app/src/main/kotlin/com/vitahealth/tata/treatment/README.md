# Treatment Management — Android

This bounded context remains inside the single Gradle module `:app`. Its
four-layer DDD/CQRS structure follows the course's native mobile architecture.
Architecture verification and device/Figma acceptance are separate gates.

## Responsibility map

```text
treatment/
  domain/
    model/entities/                Treatment, Medication
    model/valueobjects/            dosage, regimen, reminders, status
    repositories/                  domain contracts
  application/
    commands/                      mutation handlers and command inputs
    queries/                       query handlers and read inputs
    ports/                         external read capabilities
    readmodels/                    catalog and detail projections
  infrastructure/
    remote/                        Retrofit, requests, DTOs, mappers
    security/                      session ownership adapter
    di/                            TreatmentDependencies
  presentation/
    medication/
      *Route.kt                    three Route entry points
      screens/                     three Screen composables
      viewmodel/                   ViewModels, states and draft
      ui/                          medication-management components
      mappers/                     medication schedule label
    treatment/
      *Route.kt                    seven Route entry points
      screens/                     seven Screen composables
      viewmodel/                   ViewModels and states
      ui/                          treatment-detail state components
      mappers/                     treatment-detail labels
```

The application host (`app/navigation`) registers the ten destinations using
separate catalog, medication, regimen and activation graph files. The wrapper
`TreatmentNavigationGraph.kt` composes those registrations. Cross-context
navigation remains in `app`; presentation does not import `TataApplication`.

## Domain and CQRS decisions

- `Treatment` and `Medication` are domain entities. Their constructors and
  `copy()` enforce nonblank identity, adult ownership, and name/presentation.
- Dosage, frequency, administration times, instructions and reminder-delay
  constraints belong to value objects. Dedicated CommandHandlers validate
  mutation intent and orchestrate the corresponding domain repository.
- Queries read catalog/detail projections without mutating aggregate state.
  Read-oriented contracts belong to `application/ports`; repositories that
  return actual entities belong to `domain/repositories`.
- Transport response validation, error classification and DTO conversion
  remain in `infrastructure/remote`. Session ownership is enforced by
  `infrastructure/security/SessionScopedMyMedicationsOverviewReader`.
- The backend remains authoritative for state transitions, recurrence
  persistence and treatment transactions. No artificial client-side
  `AggregateRoot` or domain events are introduced for server-owned behavior.
- No `local/` or `sync/` package is created: this context does not own an
  offline treatment mutation database or durable replay queue.

## Presentation and navigation

Routes collect ViewModel state and forward callbacks; Screens render immutable
state without constructing repositories. Each of the ten existing flows has
a Route and Screen. The treatment list was divided into a state-owning Route
and stateless Screen. Existing Composable layout bodies, Figma measurements,
colors, assets, strings and callback semantics were not intentionally altered.

The host still owns `RootDestination`, argument decoding, navigation between
bounded contexts, sign-out and back-stack actions. Navigation is registered
in smaller files without changing route strings, `savedStateHandle` keys,
`popUpTo`, or destination callback behavior.

## Validation and outstanding acceptance

Verify Kotlin compilation, Android instrumentation-test compilation, JVM
regression tests and architecture-boundary rules after any change. Unit tests
cover domain invariants, commands, queries, error classification, session
isolation, adapter validation and ViewModels. Structural tests check that all
ten routes remain registered exactly once.

The following are **not** certified by green unit tests: all recurrence and
timezone cases against a running backend; process-death recovery of wizard
drafts; full device/UI interaction; pending Figma variants; runtime treatment
and medication permission transitions. Record those separately before
functional/visual sign-off.

No Git commit or push belongs to this refactor. The active plan is
`C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
