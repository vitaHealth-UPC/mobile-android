# Mobile Android Implementation Ticket Template

Use this template for implementation work in `vitaHealth-UPC/mobile-android`. It is intentionally repository-oriented and does not assume a particular person or automation tool.

~~~text
REPOSITORY
vitaHealth-UPC/mobile-android

BASE
develop

WORK BRANCH
feature/us-XX-short-description

USER STORY
US-XX — <title>

OWNING BOUNDED CONTEXT
com.vitahealth.tata.<context>

SOURCE OF TRUTH
1. Product Backlog User Story + Acceptance Criteria
2. Mobile wireflow
3. Mobile wireframe
4. Mobile mockup in VitaHealth Figma
5. docs/architecture/*
6. docs/ux/figma-frame-map.md
7. Current backend/OpenAPI contract when available
8. Course materials relevant to the feature

ARCHITECTURE
One Gradle module: :app.
Bounded Context = package under app/src/main/kotlin/com/vitahealth/tata.
Inside the BC:
domain/
application/
infrastructure/
presentation/

CQRS
Write = Command + CommandHandler.
Read = Query + QueryHandler + ReadModel.
Do not create parallel generic UseCase classes for the same behavior.

DEPENDENCY RULE
Business BC may depend on the shared package.
Business BC MUST NOT depend on another business BC.
The app package performs composition and cross-context wiring.

DOMAIN
Pure Kotlin.
No android.*, androidx.*, Compose, Retrofit or Room imports.
Business invariants live here.

APPLICATION
Orchestrate domain behavior.
Define commands/queries/handlers and ports.
Keep Android framework details out.

INFRASTRUCTURE
Room, Retrofit, DataStore, WorkManager and Android device adapters.
DTOs/entities are boundary-specific types.
Do not expose DTOs directly to Compose.

PRESENTATION
Jetpack Compose + ViewModel + StateFlow.
One-shot navigation/snackbar/permission actions use explicit UI effects.
Figma error/success/empty states are normally UiState variants, not new screens.

I18N
Default resources: English.
Locale: es-419.
Do not shrink typography merely to make a translation fit.

ACCESSIBILITY / ADAPTIVE UI
Preserve semantics, touch targets, TalkBack order and contrast.
Use Compact / Medium / Expanded layouts when the screen benefits from adaptation.

BACKEND
Do not invent REST routes.
Follow the agreed backend/OpenAPI contract when it exists.

TESTS
Domain invariant tests.
Command/Query handler tests.
ViewModel state tests.
Persistence/network tests when those boundaries change.
Compose UI tests for acceptance-critical paths when reasonable.

DOCUMENTATION
Update docs/ux/figma-frame-map.md when adding a route or UI state.
Update architecture/API docs if the feature changes an agreed boundary.

DONE
Acceptance Criteria traceable.
No cross-BC dependency.
No business rules in Compose.
New user-facing strings localized.
./gradlew test passes.
./gradlew :app:assembleDebug passes.
No secrets committed.
PR targets develop.
~~~
