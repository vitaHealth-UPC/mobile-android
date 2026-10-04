# Adherence Analytics

Gradle module: `:analytics`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/analytics/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
