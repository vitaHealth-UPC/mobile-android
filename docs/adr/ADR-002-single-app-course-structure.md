# ADR-002: Course application structure

## Status

Accepted.

## Decision

Use one Gradle module, `:app`, matching the professor's networking example. Keep the nine Bounded Contexts and `shared` as packages under `app/src/main/kotlin/com/vitahealth/tata`, preserving their domain, application, infrastructure and presentation responsibilities.

Resources and tests live in the app source sets. One dependency declaration supplies the existing Compose, Retrofit, storage and lifecycle libraries. The source boundary tests guard the logical package separation previously enforced by the build graph.

## Verification

The app unit suite, debug APK, instrumentation APK and the complete existing-screen audit instrumentation class validate the migration.
