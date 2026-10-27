# Android dependency boundaries

One Gradle module `:app`, nine bounded-context packages, an application
composition package and non-business `shared`.

```text
MainActivity / TataApplication
                |
     app/di/AppContainer
       /        |        \
 app/navigation app/integration  BC infrastructure/di
       |                 \             |
 global route             explicit     local/remote/security
 adapters                BC bridges        adapters
       |                    |
 BC presentation/navigation ---> BC application CQRS and ports
       |                              |
 BC presentation/routes/screens    BC domain
       |
 shared/presentation/designsystem

shared/application/session + shared/common/result
shared/infrastructure/network + shared/infrastructure/security
```

The app root and role back stacks are global: `app/navigation/graphs`
contains **host adapters**, not feature business logic; their references
to `RootDestination`, `AppContainer` and routes in another BC mean they
must not be copied into a BC unchanged. A BC owns its navigable content
and any self-contained registrar in `presentation/navigation`, with
host dependencies inverted into callbacks. Inventory, Preferences and
Analytics already follow this pattern.

`app/integration/medicationoverview` connects Treatment + Intake.
`app/integration/pushnavigation` connects Omission taps + Monitoring
queries. `omission/infrastructure/di` creates the actual push services;
`app/di` delegates instead of building FCM or Retrofit endpoints by hand.

`shared` has no business-specific domain and cannot import BCs.
It contains reusable session contracts, result type, network/security
adapters and a presentation design system. Intake alone owns
`intake/application/ports/SyncScheduler`. There is no productive local
event bus.

Domain business invariants stay inside each BC. Read-only Analytics
requires no commands; push-only Omission requires no Compose Screen.
No new Gradle modules or vacant layer trees.

Architecture boundary regression tests enforce this design. For functional,
device and design review see `CODEX-FUNCTIONAL-FIGMA-HANDOFF.md`.
