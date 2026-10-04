# Testing strategy

Tests mirror the architecture.

- Domain: aggregates, value objects, invariants and domain events.
- Application: CommandHandlers and QueryHandlers.
- Infrastructure: Room DAO, mappers, repositories, Retrofit contracts and synchronization.
- Presentation: ViewModels, UiState, Compose UI, navigation and accessibility.
- Architecture: no Android/Retrofit/Room imports in domain, no direct Bounded Context dependencies, and Commands/Queries in their application packages.

Architecture rules will be automated once production types exist.
