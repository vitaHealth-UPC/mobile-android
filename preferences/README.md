# Accessibility & Preferences

Gradle module: `:preferences`.

Internal structure:

```text
src/main/kotlin/com/vitahealth/tata/preferences/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

CQRS belongs to `application`. Android/Room/Retrofit types must not leak into `domain`.
