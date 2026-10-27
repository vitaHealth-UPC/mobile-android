# Inventory & Replenishment — Android

Status: **architecture normalized and covered by JVM/build gates**.
Functional device acceptance and previously unverified Figma variants are
tracked separately; this document does not claim pixel-level validation.

This BC remains a package inside the single Gradle `:app` module.

## Code ownership

```text
inventory/
  domain/
    model/entities/                InventoryBatch
    model/valueobjects/            Quantity, ReorderThreshold, InventoryLot, StockStatus
  application/
    commands/                      initial stock, replenishment
    queries/                       stock query
    ports/                         InventoryRepository
    readmodels/                    InventoryStockReadModel
  infrastructure/
    remote/                        API, DTOs, response mappers, repository
    security/                      session-scoped repository
    di/                            InventoryDependencies
  presentation/
    navigation/                    InventoryDestination, InventoryNavigationGraph
    inventory/
      InventoryRoute.kt
      screens/                     InventoryScreen, previews
      viewmodel/                   InventoryViewModel, InventoryUiState
      ui/                          form bodies, stock cards, widgets
      mappers/                     inventory error-code mapper
```

Additional request/response DTO files remain alongside the remote adapter.

## Architectural rationale

- **Domain:** `Quantity`, `ReorderThreshold` and `InventoryLot` own pure
  invariants. `InventoryBatch` validates identity, positive quantity and
  optional lot metadata, including on Kotlin `copy`.
- **Application:** Commands validate mutation intent; a read-only Query
  retrieves stock. Handlers depend on `InventoryRepository`, not Retrofit.
  The port stays in `application/ports` because it returns an application
  `InventoryStockReadModel`, not a domain aggregate.
- **Infrastructure:** Retrofit implements the port, maps error codes and
  validates backend data. The session adapter enforces user ownership
  and discards stale or cross-account responses. DI supplies a factory
  with a captured session owner.
- **Presentation:** Navigation injects a factory/callbacks. Route collects
  ViewModel state; Screen displays it. The unchanged Compose implementations
  are separated into coherent, reusable internal parts. Error-code-to-string
  mapping and Figma-state previews live outside the Screen file.

`aggregates/`, `domain/events/`, `domain/repositories/`, `local/` and
`sync/` are **intentionally absent**. Stock mutations, aggregate invariants,
transactions and low-stock decisions are server-owned; this client does not
maintain a local inventory database or a reliable offline inventory queue.
Creating empty folders, client-side fake aggregates or fabricated domain
events would obscure that responsibility.

## Acceptance and limits

- Validate US-40 initial stock and US-43 replenishment against value objects
  before making a request.
- Query US-41/US-42 uses server stock, threshold, low-stock and coverage
  data rather than client-side guesses.
- Reject malformed DTOs, duplicate batch IDs, invalid dates and invalid
  lot metadata; preserve backend status distinctions.
- Discard responses crossing session boundaries; preserve ViewModel
  cancellation, in-progress drafts and duplicate-submission guards.
- Compile the app and instrumentation tests and run Inventory JVM
  tests plus architecture-boundary tests.
- Figma visuals, navigation arguments, text resources and assets were
  intentionally not redrawn during the structural change.

**Follow-up not represented as completed:** device interaction and
pixel/Figma comparison (including pending variants), persistence of draft
inputs across process death, and server/client distinction between the
initial batch and a subsequent replenishment in the latest-batch label.
These must be explicitly verified or resolved before full functional sign-off.

See `docs/adr/ADR-001-bounded-context-modularization.md` and the
single active Android progress plan.
