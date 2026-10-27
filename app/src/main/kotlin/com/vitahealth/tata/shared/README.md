# Shared Android capabilities (not a tenth bounded context)

Shared contains **reusable**, BC-independent code. It does not mirror all
business DDD layers automatically. Never import a BC or app/ from here.

```text
shared/
  application/session/              SessionStore, SessionObserver,
                                    StoredSession, SessionOwner
  common/result/                    AppResult
  infrastructure/network/           HTTP client and response converter
  infrastructure/security/          encrypted and no-op session adapters
  presentation/designsystem/
    accessibility/                  Compose accessibility configuration
    components/                     reusable Compose controls/tabs
    theme/                          existing Figma-approved design tokens
```

No `shared/domain` directory was added: there is no actual shared clinical
aggregate. The unused no-op domain event bus was deleted rather than
described as productive. `SyncScheduler` belongs to the Intake application
port because only Intake has a sync scheduler.

The theme, typography, geometry and component appearance did not change
during package relocation. Changes to visual tokens need explicit comparison
against Figma, not subjective redesign. See ADR-002 and the Codex handoff.
