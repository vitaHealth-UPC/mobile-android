# Android user story implementation map

This document maps the 45 mobile User Stories (US-01 to US-45) to their current Android bounded contexts, application behavior and backend integrations. The mapping is based on the project's original User Story catalog and the implemented Kotlin modules. It is a functional reference, not a backlog, ticket list or branch plan.

US-46 to US-50 belong to the separate landing-page product and are outside the Android application. Figma variants describe UI states, not additional User Stories or necessarily separate screens.

## Story-to-context map

| Story | Capability | Android owner | Integration or output |
| --- | --- | --- | --- |
| US-01 | Older-adult PIN setup and access | `identity` | PIN credentials, PIN sessions |
| US-02 | Caregiver/adult linking | `carelink` | Link code and care-link acceptance |
| US-03 | Register medication | `treatment` | Medication creation |
| US-04 | Edit and deactivate medication | `treatment` | Medication update and lifecycle |
| US-05 | Scheduled dose reminder | `intake` | Next intake and reminder presentation |
| US-06 | Touch or voice dose confirmation | `intake` | Idempotent backend confirmation |
| US-07 | Missed-dose alert | `omission` / `monitoring` | Notification routing and alert display |
| US-08 | Weekly adherence summary | `analytics` | Adherence summary API |
| US-09 | Recurring omission patterns | `analytics` | Backend insight resources |
| US-10 | Caregiver registration | `identity` | Account registration |
| US-11 | Email verification | `identity` | Verification requests and confirmation |
| US-12 | Older-adult profile | `carelink` | Adult profile registration and editing |
| US-13 | Care consent | `carelink` | Care-link consent and withdrawal |
| US-14 | Create treatment | `treatment` | Treatment creation |
| US-15 | Configure dose/frequency | `treatment` | Regimen configuration |
| US-16 | Configure schedule/instructions | `treatment` | Regimen configuration |
| US-17 | Configure treatment reminders | `treatment` | Reminder lead and follow-up delay |
| US-18 | Activate or pause treatment | `treatment` | Treatment lifecycle operations |
| US-19 | Treatment detail | `treatment` | Read treatment configuration and state |
| US-20 | Next scheduled dose | `intake` | Next-intake query |
| US-21 | Dose detail by outcome | `intake` | Intake detail and mapped UI status |
| US-22 | Reinforced reminder | `intake` / `omission` | Pending-intake UI and notification routing |
| US-23 | Late dose confirmation | `intake` | Backend late/omitted distinction |
| US-24 | Daily and weekly agenda | `intake` | Time-zone-aware agenda query |
| US-25 | Recent family-care status | `monitoring` | Caregiver status resource |
| US-26 | Recent intake history | `monitoring` / `analytics` | History resources and adherence views |
| US-27 | Alert detail | `monitoring` | Protected alert detail |
| US-28 | Notification preferences | `preferences` | Channel and policy configuration |
| US-29 | Contact from an alert | `monitoring` | Contact-channel API and device launcher |
| US-30 | Caregiver follow-up note | `monitoring` | Follow-up note API |
| US-31 | Alert follow-up status | `monitoring` | Alert status transition |
| US-32 | Adherence history by period | `analytics` | Summary and trend queries |
| US-33 | Late/omitted classification | `analytics` | Read authoritative intake outcomes |
| US-34 | Adherence recommendations | `analytics` | Insights and recommendation presentation |
| US-35 | Text size | `preferences` | Local/remote accessibility preference |
| US-36 | High contrast | `preferences` | Local/remote contrast preference |
| US-37 | Reduced motion | `preferences` | Local/remote motion preference |
| US-38 | Reading assistance | `preferences` | Local/remote reading preference |
| US-39 | Quiet hours and channels | `preferences` | Notification preference API |
| US-40 | Initial inventory | `inventory` | Create stock record |
| US-41 | Stock balance | `inventory` | Inventory query |
| US-42 | Low-stock indication | `inventory` | Server low-stock flag and threshold |
| US-43 | Replenishment | `inventory` | Register stock batch |
| US-44 | Current subscription | `identity` | Plans and account subscription |
| US-45 | Change subscription | `identity` | Update account plan |

Some User Stories cross technical boundaries. The owner column identifies the Android responsibility, not a new server-side aggregate: for example, `omission` handles push-device integration while `monitoring` presents and updates caregiver alerts.

## US-01, US-10, US-11 and US-44–US-45 — Identity & Subscription

The `identity` context provides caregiver account registration, verification, password access, adult PIN access and subscription views. UI state handles form validation, authentication failures and the server's verification or lock status without treating hidden controls as an authorization mechanism.

- **US-01:** PIN setup and access use dedicated session/credential operations. PIN values are not kept in regular display preferences.
- **US-10–US-11:** registration and email verification call the account and verification resources. Backend responses determine whether verification is valid.
- **US-44–US-45:** plan catalog and subscription DTOs are mapped to application read models; changing a plan uses an authenticated account operation.

`SessionApiService`, `IdentityApiService`, `SubscriptionApiService` and the relevant command/query handlers implement these boundaries. Password recovery and social session operations are also part of this context's access architecture, although the original catalog lists no separate US number for them.

See [Identity, subscriptions and care links](../api/identity-and-care-links.md).

## US-02, US-12 and US-13 — Care Link

The `carelink` context owns adult profiles, link codes, active caregiver relationships and consent presentation. It does not store account credentials or independently authorize access to another person's clinical data.

- **US-02:** a code is generated or accepted through the care-link API; invalid or unconfirmed relationships are represented by the returned state.
- **US-12:** older-adult profile registration and authorized profile updates use the Care Link repository and remote adapters. This implementation lives in `carelink`, even though the original ticket catalog grouped US-12 with Identity.
- **US-13:** consent is registered or withdrawn through explicit commands and authenticated operations. A restricted session cannot bypass consent by navigating directly to monitoring screens.

`CaregiverProfilesApiService`, `CareLinkApiService` and the consent-withdrawal adapter handle the HTTP contracts.

See [Identity and care links](../api/identity-and-care-links.md).

## US-03–US-04 and US-14–US-19 — Treatment Management

The `treatment` context provides medication registration, editing, deactivation and treatment configuration. Domain/application types distinguish medication references, regimen input and the treatment's lifecycle; Compose screens show the corresponding read models.

- **US-03–US-04:** create, edit and change medication state without losing its persisted history.
- **US-14:** create a treatment for the selected authorized older adult.
- **US-15–US-16:** enter dosage, frequency, administration times and instructions; backend configuration is authoritative for the resulting regimen.
- **US-17:** configure reminder lead time and follow-up delay through the treatment regimen contract.
- **US-18:** activate, pause or resume through explicit lifecycle endpoints.
- **US-19:** obtain the treatment's saved configuration and status for presentation.

`TreatmentApiService` defines the medication and treatment HTTP operations. Navigation flows in `app/navigation/graphs` connect the form, detail and lifecycle screens without relocating Treatment business rules into the app shell.

See [Treatments and intakes](../api/treatments-and-intakes.md).

## US-05–US-06 and US-20–US-24 — Intake Execution

The `intake` context owns the device's dose agenda, next-dose/detail presentation, touch/voice interactions and offline confirmation coordination. The backend owns scheduling outcomes and the definitive `PENDING` / `CONFIRMED` / `LATE` / `OMITTED` status.

- **US-05, US-20 and US-22:** next-dose and pending-intake data drive scheduled and reinforced reminder presentation. Notification dispatch is integrated through `omission` rather than a separate Android clinical evaluator.
- **US-06:** touch confirmation uses `POST /api/v1/intakes/{intakeId}/confirmation`. Voice confirmation sends a mono PCM16 WAV recording through the dedicated multipart endpoint.
- **US-21 and US-23:** the detail and result screens map the server's confirmed, late and omitted outcomes. An omitted intake is not locally reclassified as confirmed.
- **US-24:** the agenda sends explicit start/end instants based on local calendar boundaries, then renders the returned dose chronology.

`IntakeApiService`, `VoiceConfirmationApiService`, the application handlers and the owner-scoped Room store implement those interactions. WorkManager replays queued touch confirmations with the same intake identifier and session owner.

See [Treatments and intakes](../api/treatments-and-intakes.md), [Push and voice](../api/notifications-and-voice.md) and [Offline synchronization](offline-sync.md).

## US-07 and US-25–US-31 — Alerts and Family Monitoring

The original ticket catalog placed several alert-detail stories under `omission`. In the implemented Android boundary, `omission` owns native push reception and notification routing; `monitoring` owns caregiver-visible alert detail, state changes, contacts and notes.

- **US-07:** a missed-dose notification links to authorized monitoring data; the app does not create the omission itself.
- **US-25–US-26:** caregiver status and recent dose history come from the backend's monitoring and intake resources.
- **US-27:** alert detail presents the recorded reason, schedule and status for the selected adult.
- **US-29:** contact-channel lookup opens the device dialer or a WhatsApp URL, instead of directly placing a call.
- **US-30:** caregiver follow-up notes are stored through the monitoring notes resource.
- **US-31:** alert status updates use the protected status endpoint and preserve the server's recorded history.

`FamilyMonitoringApiService`, `AlertsApiService` and `NotesApiService` provide these contracts. The application shell resolves notification taps to the appropriate route only after session and ownership checks.

See [Monitoring and analytics](../api/monitoring-and-analytics.md) and [Push and voice](../api/notifications-and-voice.md).

## US-08–US-09 and US-32–US-34 — Adherence Analytics

`analytics` consumes calculated period summaries, trend points, pattern evidence and recommendations from the backend. Analytics read models present confirmed, late and omitted outcomes without changing medication instructions or clinical states.

- **US-08–US-09:** weekly metrics and recurring patterns are derived from backend adherence summaries and insights.
- **US-32:** the history view changes period and renders the corresponding calculated trend and recent entries.
- **US-33:** late versus omitted classification is read from the authoritative intake outcome rather than inferred from a UI clock.
- **US-34:** recommendations are presented with the available evidence; they do not automatically change dose or treatment settings.

`AnalyticsApiService`, `AdherenceHistoryViewModel` and `AdherenceRecommendationsViewModel` coordinate queries and visual states, including insufficient-data results.

See [Monitoring and analytics](../api/monitoring-and-analytics.md).

## US-28 and US-35–US-39 — Accessibility & Preferences

`preferences` manages user-scoped accessibility and notification settings. `DataStoreAccessibilityLocalStore` preserves local selections; the remote repository maps them to backend preference resources when applicable.

- **US-28 and US-39:** notification channels, quiet hours and reminder policy come from the authenticated preferences API.
- **US-35:** text-size selection affects the Compose presentation and persisted preference.
- **US-36:** contrast settings apply to the visual scheme rather than only a label.
- **US-37:** reduced-motion selection controls nonessential animated presentation.
- **US-38:** reading assistance is represented by the corresponding preference and UI behavior.

Dark theme and voice-confirmation preference are supported by this context as additional settings, without assigning them invented story IDs.

See [Inventory and preferences](../api/inventory-and-preferences.md).

## US-40–US-43 — Inventory & Replenishment

`inventory` handles the user-facing medication stock workflow and delegates authoritative balances to the Inventory & Replenishment backend context.

- **US-40:** initial stock creation submits medication ID, positive quantity and threshold.
- **US-41:** read the stock balance, replenishment batches and available consumption estimate.
- **US-42:** render low-stock status from the backend's calculated flag and threshold.
- **US-43:** register an additional quantity and optional lot, then show the updated stock returned by the server.

`InventoryApiService` and the context's handlers map validation errors and server outcomes into inventory presentation state. The app does not debit stock merely because a dose is displayed.

See [Inventory and preferences](../api/inventory-and-preferences.md).

## Architectural and UX traceability

A User Story is realized through its owning bounded context's application behavior, infrastructure adapter and Compose presentation state. The `app` shell composes cross-context navigation; `shared` provides technical and visual primitives rather than business aggregates.

The [Figma frame map](figma-frame-map.md) connects user flows and visual states to their relevant screens. The [mobile architecture](mobile-architecture.md) describes layer ownership and CQRS dependencies, and the [API documents](../api/contract-guidelines.md) identify the HTTP resources used by the client.

This is a descriptive mapping of the Android scope. It does not define additional backend endpoints, new User Stories or separate screens for each Figma visual variant.
