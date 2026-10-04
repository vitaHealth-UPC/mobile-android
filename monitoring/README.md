# Family Monitoring

Gradle module: `:monitoring`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/monitoring/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
