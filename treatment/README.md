# Treatment Management

Gradle module: `:treatment`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/treatment/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
