# Identity & Subscription — Android

This bounded context remains within the single Gradle `:app` module. Its
four layers follow the native-mobile DDD/CQRS course, with a deliberate
distinction between server-owned identity/subscription rules and local session
or onboarding behavior.

## Structure

```text
identity/
├── domain/
│   ├── model/
│   │   ├── entities/         # CaregiverAccount, Plan, Subscription
│   │   └── valueobjects/     # account state, capabilities, change impact
│   └── repositories/         # account verification and plan contracts
├── application/
│   ├── commands/             # registration, verification, PIN, login,
│   │                         # session restore/revoke, onboarding, plan change
│   ├── queries/              # onboarding status and subscription reads
│   ├── ports/                # session access and device-local storage
│   └── readmodels/           # remembered profile and session subject
├── infrastructure/
│   ├── remote/               # REST services, responses, mapping and adapters
│   ├── local/                # installation-scoped SharedPreferences adapters
│   └── di/                   # flow factories and dependencies
└── presentation/
    ├── access/
    │   ├── SessionAccessRoute.kt
    │   ├── PinAccessRoute.kt
    │   ├── screens/
    │   ├── viewmodel/
    │   ├── ui/
    │   └── mappers/
    ├── registration/
    │   ├── CaregiverRegistrationRoute.kt
    │   ├── screens/
    │   ├── viewmodel/
    │   └── ui/
    ├── onboarding/
    │   ├── screens/
    │   └── ui/
    └── subscription/
        ├── PlanSubscriptionRoute.kt
        ├── screens/
        ├── viewmodel/
        ├── ui/
        └── mappers/
```

Only actual responsibilities receive a directory. The onboarding flow does
not need its own ViewModel merely to show an introduction screen. Access
message mapping and subscription price formatting are presentation concerns
and belong in their respective `mappers/` packages.

## Responsibilities and boundaries

- **Domain**: `CaregiverAccount`, `Plan`, and `Subscription` enforce
  required identity/plan references; a plan cannot have negative monthly
  pricing. `Subscription.impactOfChangingTo` compares capabilities.
  Server authorization, account verification and billing transitions remain
  authoritative in backend services.
- **Application**: registration, email challenge and plan change are
  Commands with dedicated handlers. Onboarding read and subscription catalog
  lookups are Queries. Access, PIN setup, session restoration and sign-out
  are Commands because they may mutate saved credentials, bind an owner
  or revoke a session. Session restoration is **not** a pure Query.
- **Contracts**: domain repositories return domain account/plan entities.
  `SessionAccessRepository`, `OnboardingRepository` and
  `RememberedAdultProfileStore` are application ports for device/session
  capabilities, not aggregate repositories.
- **Infrastructure**: Retrofit and response mappers reject invalid response
  models; shared preferences retain the existing keys. The access repository
  defends against stale, changed and expired sessions, preserving coroutine
  cancellation and explicit sign-out. Composition is in
  `IdentityDependencies`, not in a Composable.
- **Presentation**: a Route collects the ViewModel state and dispatches
  callbacks. A Screen receives immutable state and renders the current
  Figma-verified visual implementation. The registration, subscription and
  onboarding components were extracted from their original functions
  without intentional changes to dimensions, colors, strings or assets.

## Navigation

There is **no redundant `identity/presentation/navigation` package**.
The existing `app/navigation/IdentityNavigationGraph.kt` and
`SubscriptionNavigationGraph.kt` own the cross-flow destination routing.
They receive `AppContainer` from `TataNavHost` rather than reaching into
`TataApplication` themselves. Authentication, registration, PIN, onboarding
and subscription route names, argument handling and back stack actions are
unchanged.

## Testing and acceptance

Compile production and Android-test Kotlin, run JVM unit tests and apply
architecture checks. Relevant coverage includes identity/subscription
invariants, the distinct access handlers, ViewModels, authentication
cancellation, session replacement, DTO validation, local onboarding
storage semantics and host navigation composition.

The project-wide JVM/build gate does **not** certify: interactive PIN and
sign-in flows on a device, external email verification delivery, session
expiry under real device backgrounding, subscription changes against a
running backend, every Figma variant, or pixel-level screen comparison.
Those are separate functional and visual acceptance tasks.

Do not create artificial `aggregates/`, `events/`, `sync/` or
`domain/services/` folders to imitate a template. No Gradle module split
and no changes to persisted keys, credentials, route arguments or visual
design belong to this architecture task.

Canonical working plan:
`C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
