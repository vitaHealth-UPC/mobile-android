# Mobile Android User Story Master Tickets

This document is the implementation contract for `vitaHealth-UPC/mobile-android`. It translates the Product Backlog, the mobile UX artifacts, and the agreed DDD/CQRS architecture into repository-level work without treating every Figma frame as a separate screen.

## Scope and source of truth

Implementation decisions follow this order:

1. Product Backlog User Story and Acceptance Criteria.
2. Mobile wireflow.
3. Mobile wireframe.
4. Mobile mockup in the VitaHealth Figma file.
5. Current repository architecture and bounded-context map.
6. Course materials for Android, DDD/CQRS, Room, networking and adaptive UI.
7. The agreed backend/OpenAPI contract when it exists.

Behavior comes from the backlog and Acceptance Criteria. Figma governs visual representation and states. A Figma error/success/empty variant is normally a `UiState` of the same screen, not a new screen class.

## Branch matrix

| US | Branch | Owning module | Main responsibility |
| --- | --- | --- | --- |
| US-01 | `feature/us-01-pin-access` | `:identity` | PIN setup/access, incorrect PIN and lockout |
| US-02 | `feature/us-02-care-link` | `:carelink` | Family/caregiver link flow |
| US-03 | `feature/us-03-register-medication` | `:treatment` | Register medication |
| US-04 | `feature/us-04-update-deactivate-medication` | `:treatment` | Edit/deactivate medication |
| US-05 | `feature/us-05-dose-reminder` | `:intake` | Scheduled dose reminder |
| US-06 | `feature/us-06-dose-confirmation` | `:intake` | Touch/voice dose confirmation |
| US-07 | `feature/us-07-missed-dose-alert` | `:omission` | Missed-dose alert representation |
| US-08 | `feature/us-08-weekly-adherence-summary` | `:analytics` | Weekly adherence metrics |
| US-09 | `feature/us-09-recurring-omission-pattern` | `:analytics` | Recurring omission patterns |
| US-10 | `feature/us-10-caregiver-registration` | `:identity` | Caregiver account registration |
| US-11 | `feature/us-11-email-verification` | `:identity` | Email verification lifecycle |
| US-12 | `feature/us-12-older-adult-profile` | `:identity` | Older-adult profile |
| US-13 | `feature/us-13-care-consent` | `:carelink` | Care-link consent |
| US-14 | `feature/us-14-create-treatment` | `:treatment` | Treatment creation |
| US-15 | `feature/us-15-dose-frequency` | `:treatment` | Dosage/frequency |
| US-16 | `feature/us-16-schedule-instructions` | `:treatment` | Schedule/instructions |
| US-17 | `feature/us-17-treatment-reminders` | `:treatment` | Treatment reminder policy |
| US-18 | `feature/us-18-treatment-activation-pause` | `:treatment` | Activate/pause treatment |
| US-19 | `feature/us-19-treatment-detail` | `:treatment` | Treatment detail |
| US-20 | `feature/us-20-next-dose` | `:intake` | Next dose |
| US-21 | `feature/us-21-dose-detail` | `:intake` | Dose detail by state |
| US-22 | `feature/us-22-reinforced-reminder` | `:intake` | Reinforced reminder |
| US-23 | `feature/us-23-late-dose-confirmation` | `:intake` | Late confirmation/tolerance |
| US-24 | `feature/us-24-daily-schedule` | `:intake` | Daily/weekly schedule |
| US-25 | `feature/us-25-recent-care-status` | `:monitoring` | Family summary/recent care |
| US-26 | `feature/us-26-intake-history` | `:analytics` | Recent intake history |
| US-27 | `feature/us-27-alert-detail` | `:omission` | Alert detail |
| US-28 | `feature/us-28-notification-preferences` | `:preferences` | Notification preferences |
| US-29 | `feature/us-29-alert-contact` | `:omission` | Contact action from alert |
| US-30 | `feature/us-30-followup-note` | `:omission` | Caregiver follow-up note |
| US-31 | `feature/us-31-alert-followup` | `:omission` | Mark alert attended/follow-up |
| US-32 | `feature/us-32-adherence-history` | `:analytics` | Adherence history by period |
| US-33 | `feature/us-33-late-omitted-classification` | `:analytics` | Consume late/omitted classification |
| US-34 | `feature/us-34-adherence-recommendations` | `:analytics` | Recommendations with evidence |
| US-35 | `feature/us-35-text-size` | `:preferences` | Large text |
| US-36 | `feature/us-36-high-contrast` | `:preferences` | High contrast |
| US-37 | `feature/us-37-reduced-motion` | `:preferences` | Reduced motion |
| US-38 | `feature/us-38-reading-assistance` | `:preferences` | Reading assistance |
| US-39 | `feature/us-39-quiet-hours-channels` | `:preferences` | Quiet hours/channels |
| US-40 | `feature/us-40-initial-inventory` | `:inventory` | Initial inventory |
| US-41 | `feature/us-41-stock-status` | `:inventory` | Stock status |
| US-42 | `feature/us-42-low-stock-alert` | `:inventory` | Low-stock state |
| US-43 | `feature/us-43-restock` | `:inventory` | Replenishment |
| US-44 | `feature/us-44-current-plan` | `:identity` | Current plan/subscription |
| US-45 | `feature/us-45-change-subscription` | `:identity` | Change subscription |

US-46 to US-50 belong to the Landing Page product and are intentionally outside this repository.

## Architectural contract

Each business Bounded Context is a Gradle module:

```text
:identity
:carelink
:treatment
:intake
:omission
:monitoring
:analytics
:inventory
:preferences
```

Support modules:

```text
:app    -> composition root, root navigation, role-aware shell, cross-context wiring
:shared -> technical/design primitives only; never shared business aggregates
```

Inside every business module:

```text
domain/
application/
infrastructure/
presentation/
```

### Layer responsibilities

**Domain**
- Pure Kotlin business model.
- Aggregates, entities, value objects, domain services, events and repository contracts.
- Must not import Android, Compose, Retrofit, Room or other business modules.

**Application**
- CQRS orchestration.
- Writes: `Command + CommandHandler`.
- Reads: `Query + QueryHandler + ReadModel`.
- Do not create parallel generic `UseCase` classes for the same responsibility.

**Infrastructure**
- Room/SQLite adapters.
- Retrofit/OkHttp adapters.
- DataStore, WorkManager and Android device adapters where appropriate.
- DTOs/entities remain infrastructure details.

**Presentation**
- Jetpack Compose.
- ViewModel + StateFlow for persistent UI state.
- One-shot events use an explicit UI effect stream.
- Figma variants are usually state variants, not duplicate screens.

### Dependency rules

- A business module may depend on `:shared`.
- A business module must not depend directly on another business module.
- `:app` performs composition and cross-context wiring.
- Cross-context information uses backend read models, ports/ACLs, or routed events; never another module's DAO/repository.
- Domain models are not Retrofit responses, Room entities or UI models.

### i18n and accessibility

- Base resources are English.
- Supported localization is `es-419`.
- A longer translation must be handled by layout, not by arbitrarily shrinking typography.
- Accessibility settings must affect real behavior: text scale, contrast, reduced motion, voice confirmation and reading assistance.
- Standard semantics, content descriptions, focus order, touch targets and TalkBack behavior remain mandatory.

### Adaptive UI

Use window size rather than device-name assumptions:

```text
Compact   < 600dp
Medium    600-839dp
Expanded  >= 840dp
```

Where useful, list/detail flows should become side-by-side in Expanded layouts rather than merely scaling the Compact screen.

---

# Identity & Subscription — `:identity`

## US-01 — PIN access

**Goal:** configure and authenticate an older-adult PIN without exposing the secret.

**Domain**
- `PinCredential`
- `PinAttemptPolicy`
- `PinAuthenticationResult`
- `Session`
- identifiers for account/older adult

**Application**
- `ConfigurePinCommandHandler`
- `AuthenticateWithPinCommandHandler`
- `GetPinAccessStatusQueryHandler`

**Infrastructure**
- secure local credential/session adapter
- Android Keystore-backed secret handling where appropriate
- no plaintext PIN in logs/storage

**Presentation**
- one `PinAccessScreen`
- states: create, ready, incorrect, temporarily locked

**Tests**
- valid/invalid PIN
- attempt threshold
- lock persistence through recreation
- no secret leakage

## US-10 / US-11 / US-12 — registration, verification and profile

**Domain**
- `CaregiverAccount`, `EmailAddress`, verification state
- `OlderAdultProfile`, `EmergencyContact`

**Application**
- `RegisterCaregiverCommandHandler`
- `VerifyEmailCommandHandler`
- `RequestNewVerificationCommandHandler`
- `CreateOlderAdultProfileCommandHandler`

**Presentation states**
- registration editing/submitting/pending verification/duplicate email/failure
- verification valid/expired/request-new-code
- profile editing/saved/error

The backend remains the authority for duplicate email and verification validity.

## US-44 / US-45 — plan and subscription

**Domain/read model**
- `Plan`
- `Subscription`
- `PlanCapability`

**Application**
- `GetCurrentSubscriptionQueryHandler`
- `ChangeSubscriptionCommandHandler`

**Rules**
- capabilities are data, not string checks in Compose
- current/renewal/status semantics come from the domain/API
- "subscription updated" is an effect/state of the same flow, not a separate screen

---

# Care Link — `:carelink`

## US-02 — care link

**Domain**
- `CareLink`
- `LinkCode`
- `LinkCodeStatus`
- `CareLinkStatus`

**Application**
- request/submit link-code commands
- care-link status and linked-person queries

**Presentation**
- link/consent flow
- linked-person view
- states: entering code, pending consent, invalid/expired code, linked

The module receives external account/older-adult identifiers from composition/session. It does not import `:identity`.

## US-13 — care consent

**Domain**
- `Consent`
- `ConsentStatus`
- consent-recorded event

**Application**
- accept/reject care-link commands
- consent-status query

**Critical state**
- `ConsentRequired` / restricted monitoring

Authorization must be enforced by repository/API semantics, not only by hiding UI.

---

# Treatment Management — `:treatment`

## Aggregate model

```text
Treatment
├── TreatmentId
├── OlderAdultId
├── TreatmentStatus
└── Medication[]
    ├── MedicationId
    ├── Dosage
    ├── Frequency
    ├── AdministrationSchedule
    ├── AdministrationInstructions
    └── ReminderPolicy
```

Treatment states include `INCOMPLETE`, `ACTIVE`, `PAUSED`. Medication states include `ACTIVE`, `INACTIVE`.

## US-03 / US-04 — medication lifecycle

**Application**
- `RegisterMedicationCommandHandler`
- `UpdateMedicationCommandHandler`
- `DeactivateMedicationCommandHandler`

**Presentation**
- add/edit/detail UI
- required-field error state
- active/updated/inactive state

**Rule:** deactivation preserves history.

## US-14 to US-18 — treatment configuration and lifecycle

- US-14: `CreateTreatmentCommandHandler`; incomplete treatment is not activated.
- US-15: dosage/frequency are value objects, not unvalidated display strings.
- US-16: schedule/instructions support one or multiple times.
- US-17: reminder policy affects future behavior, not historical outcomes.
- US-18: activate/pause commands and domain events preserve treatment history.

## US-19 — treatment detail

Use a `TreatmentDetailReadModel` with medication, dosage, frequency, schedules, instructions and status.

Expected states: loaded, restricted access, not found.

Do not create a separate `TreatmentPausedScreen` solely because Figma contains a paused variant.

---

# Intake Execution — `:intake`

## Domain model

- `ScheduledDose`
- `DoseId`
- `DoseStatus`: pending, confirmed, late, omitted
- `ToleranceWindow`
- `DoseConfirmation`
- `ConfirmationChannel`: touch/voice
- `ReminderSchedule`

## US-05 / US-22 — reminders

- schedule reminders for unresolved doses
- reinforced reminders only while the dose remains pending
- WorkManager is for appropriate background/retry work; do not assume it is a universal exact-alarm replacement

## US-06 — confirmation

Prefer one `ConfirmDoseCommand(doseId, channel)` with a single handler/invariant.

**Critical rule:** retries or multiple input channels must not create duplicate confirmations or duplicate side effects.

Voice recognition is behind an application/device port. Request `RECORD_AUDIO` only when the user starts the voice flow.

## US-20 / US-21 / US-24 — read side

- next-dose query and no-next-dose state
- dose detail read model with pending/confirmed/late/omitted presentation states
- daily/weekly schedule query sorted chronologically

## US-23 — late confirmation

The domain/backend decision controls tolerance:
- confirmation within tolerated late window -> late outcome
- after definitive omission -> preserve omitted history unless the contract explicitly says otherwise

Do not derive this business rule from Compose labels/colors.

---

# Omission & Escalation — `:omission`

## Domain model

- `Alert`
- `AlertId`
- `AlertReason`
- `AlertStatus`
- `FollowUpNote`
- `Intervention`
- `ContactChannel`

## US-07 / US-27 — missed dose and alert detail

The mobile app represents/consumes missed-dose alerts from the backend/domain flow rather than independently reinventing omission rules.

Alert detail includes medication, scheduled time, outcome/reason and follow-up history.

## US-29 — contact action

Use an Android device abstraction/adapter for dialer or messaging. Prefer launching the dialer unless direct calling is explicitly required.

States: contact available / contact unavailable.

## US-30 / US-31 — follow-up

- add a follow-up note with author, timestamp and content
- mark alert attended without deleting its history

Figma states such as pending, note saved, unavailable contact and attended are states of the same alert-detail flow.

---

# Family Monitoring — `:monitoring`

## US-25 — recent care status

This is deliberately read-heavy.

A `FamilySummaryReadModel` can include:
- older adult summary
- next dose
- recent dose outcomes
- active alert summary
- adherence summary

Do not implement this by importing `:intake`, `:omission` or `:analytics`. Use a backend consolidated read model or a projection composed outside those module boundaries.

---

# Adherence Analytics — `:analytics`

## Domain/read models

- `AdherencePeriod`
- `AdherenceMetrics`
- `DoseOutcomeSummary`
- `AdherencePattern`
- `AdherenceRecommendation`

## US-08 — weekly adherence

Read model includes confirmed, late, omitted, total and percentage. Include insufficient-data state.

## US-09 — recurring omission pattern

Detected patterns must carry evidence that can be represented to the user.

## US-26 / US-32 / US-33 — history and classification

- recent intake history
- adherence history by selected period
- late/omitted classification is consumed from Intake/backend outcomes rather than re-invented in Analytics

Expected states include no data, no results and period updated.

## US-34 — recommendations

Recommendations may guide reminders, scheduling consistency or follow-up. They must not modify dosage, medication or clinical instructions.

States: recommendations available / insufficient evidence.

---

# Accessibility & Preferences — `:preferences`

DataStore is the preferred persistence mechanism for simple user preferences.

## Model

```text
AccessibilityPreferences
- textScale
- highContrast
- reducedMotion
- voiceConfirmation
- readingAssistance

NotificationPreferences
- enabledCategories
- enabledChannels
- quietHours
```

## US-35 to US-38 — accessibility

- text scale feeds the shared design system rather than manually editing every `Text`
- high contrast changes the color scheme coherently
- reduced motion provides alternatives for non-essential transitions
- reading assistance must have real supported behavior, not a decorative toggle

Figma variants are states of `AccessibilityScreen`.

## US-28 / US-39 — notifications

Commands/queries persist categories, channels and quiet hours. `NotificationPreferencesScreen` represents editing/saving/error states.

`:shared` must not depend on `:preferences`. Shared abstractions may be implemented by `:preferences` and injected by `:app`.

---

# Inventory & Replenishment — `:inventory`

## Domain model

- `Inventory`
- `InventoryId`
- `StockQuantity`
- `ReorderThreshold`
- `Replenishment`
- `LotInfo`

## US-40 — initial inventory

Quantity must be non-negative. Invalid quantity is a state/validation error, not a separate screen.

## US-41 / US-42 — stock status and low stock

Read model may expose remaining units and estimated days remaining. Low-stock evaluation is based on domain data/threshold, not a visual constant.

## US-43 — replenishment

Register quantity, lot and timestamp transactionally. A successful replenishment updates stock and the low-stock condition consistently.

---

# Shared design system

Repeated visual primitives belong in `:shared/design` when they are genuinely reusable and contain no business rule.

Examples:

```text
TataButton
TataTopBar
TataBottomBar
TataCard
MedicationCard
DoseCard
StatusCard
MetricCard
InlineMessage
EmptyState
PinPad
ProgressStepper
ToggleRow
```

A business-specific model remains owned by its Bounded Context even when a generic visual component is reused.

---

# Figma mapping

Keep `docs/ux/figma-frame-map.md` updated with:

```text
Figma Frame | User Story | Bounded Context | Route | Screen | UiState
```

The expected flow is:

```text
User Story
  -> Acceptance Criteria
  -> Wireflow
  -> Wireframe
  -> Mockup
  -> UiState
  -> Compose implementation
```

Do not implement entire mockup screenshots as images.

---

# Backend contract rule

Do not invent production REST routes in Android before the backend/OpenAPI contract exists.

It is acceptable to define application/repository interfaces and request/response types that the mobile feature needs, but Retrofit annotations and route shapes must follow the agreed contract once available.

---

# Data and synchronization

Typical ownership:

**Room / structured local data**
- treatments/medications
- schedules/intake records
- alert cache
- inventory cache
- pending synchronization records

**DataStore / preferences**
- language
- text size
- contrast
- motion
- reading assistance
- notification preferences
- current role where appropriate

Sensitive credentials/tokens must not be stored as plaintext preferences.

A DAO owned by one Bounded Context must not directly query another Bounded Context's logical tables.

---

# Testing contract

Each feature adds the smallest appropriate set of tests across the changed boundaries:

- Domain invariants/value objects/events.
- CommandHandler and QueryHandler behavior.
- ViewModel state transitions.
- Room DAO/mappers/repositories when persistence changes.
- Retrofit mapping with a test server when networking changes.
- Compose UI tests for acceptance-critical paths where reasonable.
- Accessibility/adaptive-layout tests for relevant screens.

Architecture checks should preserve:
- no Android/Retrofit/Room/Compose imports in Domain;
- no direct business-module-to-business-module dependency;
- no DAO/Retrofit creation inside ViewModels;
- naming/location rules for Commands and Queries.

---

# Definition of Done for every mobile US

1. Acceptance Criteria are traceable to code/tests.
2. Business rules are in Domain/Application, not in Composables.
3. Writes use Commands/CommandHandlers where CQRS applies.
4. Reads use Queries/QueryHandlers/ReadModels where CQRS applies.
5. Domain, DTO, Entity, ReadModel and UiModel boundaries are not collapsed without reason.
6. Normal/error/empty/success/blocked UX states are represented.
7. Figma variants are not duplicated into unnecessary Screen classes.
8. English base resources and `es-419` translation are present for new user-facing strings.
9. No direct dependency/import between business Bounded Contexts.
10. No DAO/Retrofit client is created in a ViewModel/Composable.
11. StateFlow represents observable UI state.
12. Android permissions are requested in context.
13. Adaptive behavior is considered for Compact, Medium and Expanded widths where useful.
14. Relevant Compose previews cover representative Figma states.
15. Domain/Application tests cover new invariants/handlers.
16. ViewModel tests cover critical UI transitions.
17. Persistence/network tests are added when those boundaries change.
18. `./gradlew test` passes.
19. `./gradlew :app:assembleDebug` passes.
20. `docs/ux/figma-frame-map.md` is updated when a route/state is added.
21. Backend endpoints are not invented.
22. No credentials/secrets are committed.
23. Branch is current with `develop`.
24. Pull request targets `develop`.
