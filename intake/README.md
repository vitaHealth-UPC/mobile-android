# Intake Execution

Gradle module: `:intake`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/intake/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
