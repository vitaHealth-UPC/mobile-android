# ADR-002: Application shell, shared primitives and navigation ownership

**Status:** Accepted, 2026-10-24.
**Supersedes:** only the placement of transverse facilities discussed in ADR-001.
**Scope:** Android single Gradle `:app` codebase.

## Context and decision

The Android DDD/CQRS course describes independent responsibilities, not an
obligation to make `app`, `shared` or every feature a separate Gradle module.
Google Android's modular navigation guidance places the root host and global
navigation in the app module, while feature-owned destinations may register
their own composable content. Here the equivalents are Kotlin package
boundaries because the project uses one `:app` by design.

`app` is **not a bounded context**. It owns the process entry, root
composition, global role navigation, route compatibility and explicit
integration between independent BCs. It may **depend on** their public
composition factories but must not reimplement their adapters or business
policies. Keep these folders:

```text
app/
  MainActivity.kt
  TataApplication.kt
  SpanishAppContext.kt
  di/AppContainer.kt
  integration/
    medicationoverview/MedicationOverviewIntegration.kt
    pushnavigation/{PushAlertResolver,PushAlertSelection}.kt
  navigation/
    TataNavHost.kt
    CaregiverTabNavigation.kt
    destinations/RootDestination.kt
    deeplink/DeepLinkHandler.kt
    graphs/                         host adapters for global routes
  shell/                            role-specific scaffolds and push entry
```

### Why feature-named host graphs remain under app/navigation/graphs

An adapter such as `CareLinkNavigationGraph.kt` navigates from CareLink to
Identity, Treatment or Monitoring, reads the app-wide route contract and
takes an `AppContainer` provided by the root. Moving that adapter verbatim
into a BC would import `app` from the BC, violating the dependency rule.
Therefore **feature-named is not automatically feature-owned**.

Pure in-feature registration, where routes and screens can be registered
without knowing the app host, belongs in the BC
`presentation/navigation`. Existing examples are Analytics, Inventory
and Preferences. If another graph later becomes self-contained, first
invert its host dependencies through factory/callback parameters, then
migrate it safely. Never duplicate `NavHost` or change route strings,
argument encoding, stack policy or deep-link compatibility for a
cosmetic folder cleanup.

The app's own host receives `AppContainer` explicitly. Treatment global
graphs no longer retrieve `TataApplication` through Compose `LocalContext`.
`MainActivity` is a valid Android process entry and may use its
`application` property to obtain the container.

### Where Omission and inter-context collaboration live

`omission/infrastructure/di/OmissionDependencies.kt` assembles Android
FCM adapters, push topic subscriptions, device installation ID and remote
token registry, notification owner policy and session cleanup. `AppContainer`
holds one facade and delegates; it has no Firebase repository constructor.

`app/integration/medicationoverview` composes Treatment medication catalog
queries with Intake next-dose projections. `app/integration/pushnavigation`
maps Omission notification taps to existing Monitoring alert queries.
Neither bounded context imports the implementation of the other.
The app shell owns the cross-context request and never creates clinical
aggregates from notification payloads.

### Shared is not a tenth bounded context

The shared package contains **reusable contracts, adapters and visuals**,
not clinical aggregates or an all-purpose domain model:

```text
shared/
  application/session/               SessionOwner, StoredSession,
                                     SessionStore, SessionObserver
  common/result/                     AppResult
  infrastructure/network/            ApiClientFactory,
                                     JsonResponseBoundaryConverterFactory
  infrastructure/security/           EncryptedSessionStore, NoSessionStore
  presentation/designsystem/
    accessibility/                   TataAccessibility
    components/                      accessible composable primitives, tabs
    theme/                           color/spacing/typography and tokens
```

A `shared/domain` directory is not created: there is no genuine domain
aggregate shared by several BCs. `NoSessionStore` is an Infrastructure
adapter rather than an application port. `SyncScheduler` is an Intake
application port because its only consumer is Intake. The former
`DomainEvent`, `DomainEventPublisher` and `NoOpDomainEventPublisher` were
unreferenced and removed instead of presenting a non-functional event
pipeline; the unused `SyncResult` was removed too.

The app preserves the approved design system's values and Compose bodies;
moving a theme or component changes its package only, not its appearance.

## Dependency rules

- `shared` cannot import `app` or any bounded context.
- BCs may use `shared`; cannot import `app` nor another BC's internals.
- `app` can assemble BCs, but only through their application interfaces
  and infrastructure `di` facades or **explicit app-owned adapters**.
- Inside a BC: Presentation to Application, Domain pure, Infrastructure
  implements domain/application ports; CQRS read models are not domain
  aggregates. Omission push-only and Analytics read-only remain exceptions
  to unnecessary empty folders.
- One root `NavHost`: the app owns cross-BC destinations and callback
  coordination, while self-contained destination content is feature-owned.
- Session owner and expiry must be preserved on all remote/local/push paths.

## Validation and limitations

The refactor is validated by Kotlin production compilation,
AndroidTest-source compilation, JVM unit tests, architectural source
guards and `git diff --check`. These are architecture checks, **not**
a claim of functioning FCM transport, accessibility, Figma pixel parity,
approved backend transitions or live account scenarios.

**Important open risk:** FCM legacy topic subscription can outlive an
account session. Rejecting local display is necessary but insufficient
as a guarantee about message transport. Verify authorized delivery or
unsubscription with the backend in the next phase.

## References

- APMO `1ACC0238-Native_Mobile_DDD_CQRS.pdf` slides 5/7/10-11.
- APMO Android Architecture Flow, Android Data Architecture.
- Android: https://developer.android.com/topic/modularization/patterns
- Android: https://developer.android.com/guide/navigation/integrations/multi-module
- `docs/architecture/CODEX-FUNCTIONAL-FIGMA-HANDOFF.md`
