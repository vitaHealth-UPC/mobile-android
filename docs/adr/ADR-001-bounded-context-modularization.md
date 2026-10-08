# ADR-001: Bounded Context modularization

## Status

Superseded by [ADR-002](ADR-002-single-app-course-structure.md).

## Decision

Use one Gradle module per canonical Bounded Context. Inside each context, keep four DDD layers:

```text
presentation -> application -> domain
infrastructure -------------> domain
```

Application uses CQRS Commands/CommandHandlers and Queries/QueryHandlers. The `:app` module is the composition root and `:shared` contains only genuinely shared technical/design primitives.

## Consequences

- Domain boundaries are visible in the build graph.
- A feature is owned by a domain context rather than by a global `screens` package.
- Cross-context communication must use explicit contracts/events instead of direct imports.
- Figma frames representing alternative states remain states of screens, not independent architecture modules.
