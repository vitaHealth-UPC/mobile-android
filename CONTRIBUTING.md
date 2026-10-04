# Contributing

## Branches

- `main`: stable baseline.
- `develop`: integration branch.
- `feature/us-xx-description`: User Story implementation.
- `fix/us-xx-description`: defect tied to a story/state.
- `release/x.y.z`, `hotfix/x.y.z`: release maintenance when needed.

## Commit examples

```text
feat: add treatment creation flow
fix: preserve omitted dose state
refactor: isolate treatment read model
test: cover pin lockout state
docs: map figma frames to intake states
chore: configure room dependencies
```

## Architectural constraints

1. Bounded Context modules must not depend directly on other Bounded Context modules.
2. Domain code must not import Android, Retrofit or Room types.
3. Presentation must not instantiate Retrofit services or DAOs.
4. Commands and Queries belong to the Application layer.
5. DTOs, Room entities and UI models must remain separate from domain models.
