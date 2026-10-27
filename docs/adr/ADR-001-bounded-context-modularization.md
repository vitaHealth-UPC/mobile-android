# ADR-001: Android bounded contexts, DDD layers and CQRS

## Status

Accepted. Revised on 2026-10-24 to clarify semantic ownership,
dependencies and the incremental CQRS migration.

## Source alignment and decision

The course's `1ACC0238-Native_Mobile_DDD_CQRS.pdf` describes bounded
contexts and four logical layers: Domain, Application, Infrastructure and
Presentation, with Commands/CommandHandlers and Queries/QueryHandlers.
The Android Architecture Flow diagram presents a three-column view at a
different abstraction level: its UseCase maps to our Application layer, its
business Models map to Domain, and its DataRepository/data sources map to
Infrastructure. The ViewModel belongs to Presentation.

The nine bounded contexts remain Kotlin packages under a single Gradle
module, `:app`. This is deliberate; bounded contexts are not required to
be independent Gradle modules in this project. Their boundaries are
enforced through package dependencies, ownership and architecture tests.

## Layer responsibilities

| Layer | Owns | Excludes |
| --- | --- | --- |
| `domain` | Business entities, aggregates when justified, immutable value objects, invariants, policies, domain repository contracts | Android, Compose, DTO, Retrofit, Room, UI state |
| `application` | Commands and Queries, dedicated handlers, read models, application ports and use-case coordination | Screen rendering, transport implementation, UI-specific formatting |
| `infrastructure` | Repository adapters, remote/local data sources, DTO mapping, Room, DataStore, WorkManager, DI bindings | Presentation decisions and business rules that should live in domain |
| `presentation` | Compose Route/Screen, ViewModel, UiState, UI models/mappers and context-local navigation | HTTP calls, DAOs, cross-context orchestration and direct infrastructure access |

`app` owns application startup, host navigation and composition between
contexts. `shared` owns reusable technical or design primitives, never
business aggregates shared merely to circumvent bounded-context boundaries.

## CQRS and contracts

- A Command represents intent to change state. A CommandHandler validates
  that intent and coordinates domain operations or server-owned mutations.
- A Query is read-only. A QueryHandler may return an application read model
  without constructing or mutating a domain aggregate.
- Use `application/commands` and `application/queries` for separately named
  commands/queries and their handlers. `application/readmodels`,
  `application/ports` and `application/inputs` exist only when justified.
  This replaces the historical flat `application/usecases` after a semantic
  audit of each context; do not move all source files mechanically.
- Domain repositories describe operations on domain entities and aggregates.
  Application ports describe read-model projections and external capabilities.
  Concrete implementations live in infrastructure in either case.
  Avoid duplicating the same repository interface in both layers.
- Pure domain value objects own their invariants. Handlers reject invalid
  commands before calling a port. Infrastructure validates incoming transport
  data; backend transactions and aggregates remain authoritative.
- No compulsory AggregateRoot, DomainService or event publisher should be
  created solely to fill a package. The Android `NoOpDomainEventPublisher`
  was removed because it had no producers or consumers; do not recreate it as a placeholder.

## Presentation structure

The course requires separated presentation responsibilities without
mandating specific folder names. Audited contexts group the top-level flow
under `presentation/<flow>/`, and subdivide its actual implementations into
`screens/`, `viewmodel/`, `ui/` and `mappers/` as needed. Route remains at the
flow root and gathers ViewModel state; Screen renders state and callbacks.

Inventory, Treatment, Identity, Care Link, Intake, Monitoring, Preferences and Analytics follow that convention
after context-specific refactors and successful Kotlin/JVM compilation gates.
Intake keeps its owner-scoped Room storage and pending-confirmation replay
inside Infrastructure; its Agenda ViewModel obtains data through a QueryHandler.
Monitoring groups family summary, alerts (list/detail), caregiver notes
and personal notes by flow. Shared contact actions stay under
presentation/contact. Its ViewModels use CQRS handlers, while session
ownership is implemented by infrastructure/security.
Preferences keeps its two feature flows (accessibility and notifications)
with Route, Screen, ViewModel, UI components and meaningful mappers.
It retains a self-contained presentation/navigation registrar because
that registrar accepts host-owned factories and callbacks without
importing AppContainer or any other context. DataStore remains local;
session ownership and offline-first reconciliation remain infrastructure
responsibilities in security/ and sync/, respectively.
Analytics is a read-only bounded context: commands/ is intentionally absent.
It retains its own navigation graph for history and recommendations and
separates Screen, Route, ViewModel, flow-local UI, and presentation mappers.
Authorization/session ownership belongs in infrastructure/security; UI
charts and projections are never domain aggregates.

Omission is a push-only bounded context: it has application commands,
queries, ports, policies and a registration coordinator, not Compose
screens or ViewModels. Its native notification UI and intent extras belong
under presentation/notifications; the FCM service is explicitly declared
in the Android manifest. Monitoring owns caregiver alert list/detail,
status and follow-up; the backend owns omission/escalation transitions.

Do not create empty folders or move unrelated contexts mechanically just to
match this visual tree. All nine bounded contexts have been audited
individually: this is an architecture checkpoint, not device/backend/Figma
acceptance. Figma-approved Compose dimensions and assets are not reasons
to redraw validated screens. Where ViewModels exist they own UI state;
Compose owns rendering, and infrastructure stays behind application ports.

Navigation inside a context uses its own destinations and callbacks;
cross-context routing is composed by `app`. A Route obtains a factory
through dependency injection; it must not create Retrofit/Room instances
or business repositories itself. Figma variants can share a Screen and
differ only by state.

## Scope, ownership and safety

- Family Monitoring owns caregiver-facing alert queries, detail, status
  updates and follow-up (backend TS09). Omission & Escalation owns
  omission generation/escalation and push delivery (backend TS05/TS06).
- Preserve UI geometry, accessibility, language strings, assets, route
  arguments, back-stack behavior and Figma validations during this phase.
- Preserve applicationId, Room entity packages/schema, persisted keys,
  session ownership and offline synchronization semantics.
- Keep Kotlin readable: four-space indentation, explicit imports, blank
  lines, meaningful names and focused KDoc on responsibilities/invariants.
  Do not add boilerplate KDoc to trivial private implementation details.

## Incremental acceptance

1. Classify a BC's types as entity, aggregate, value object, read model,
   DTO, command/query, handler, repository contract or adapter.
2. Correct genuine responsibility and dependency violations.
3. Update all consumers and tests with each move; preserve source behavior.
4. Pass unit and Android test-compilation gates; run device and Figma checks
   separately. Compilation is not proof of UI fidelity.
5. Record verified changes in the single active plan. Do not stage, commit,
   push or reset while accumulating the user's planned single commit.

## Course and project references

- `1ACC0238-Native_Mobile_DDD_CQRS.pdf`, especially slides 5, 7, 10-11.
- `1ACC0238-Android_Data_Architecture.pdf` and the Android Architecture Flow.
- `docs/architecture/bounded-context-map.md`.
- `C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.

## Presentation subpackages within each audited flow (2026-10-24)

Identity & Subscription keeps `access`, `registration`, `onboarding` and
`subscription` as the primary presentation boundaries. Each flow then uses
`screens/`, `viewmodel/`, `ui/` and `mappers/` only when those
responsibilities exist; its Route entry point remains at the flow root.

This is **not** a requirement to create empty directories. A static onboarding
screen does not need a ViewModel solely for uniformity. Screens are stateless
renderers; their Route collects application state and forwards callbacks.
ViewModels depend on application Command/Query handlers rather than
infrastructure adapters or repository ports.

Navigation packages are equally responsibility-driven. Identity and Treatment
already have destination registration coordinated by the application host in
`app/navigation`. Neither must recreate those registrations inside its
bounded context, and no redundant `presentation/navigation/` is required.
A context-local navigation interface is warranted only when a feature owns
internal transitions that can be described without importing `app`.

## Cross-cutting architecture checkpoint

App shell, shared layers, FCM composition and root vs feature navigation
were normalized after the nine BC audits. ADR-002 records the exact
responsibility split; the current user-facing task for Codex is the
functional/Figma review in
`docs/architecture/CODEX-FUNCTIONAL-FIGMA-HANDOFF.md`.
