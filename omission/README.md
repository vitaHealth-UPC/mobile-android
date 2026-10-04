# Omission & Escalation

Gradle module: `:omission`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/omission/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
