# Inventory & Replenishment

Gradle module: `:inventory`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/inventory/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
