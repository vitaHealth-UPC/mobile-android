# Figma frame map

This file is the implementation traceability map between UX states, User Stories and code.

| UX area/state | Bounded Context | Screen/state |
| --- | --- | --- |
| Onboarding, registration, verification | identity | `CaregiverRegistrationScreen` + account / verification / verification-expired / complete states |
| PIN access, incorrect PIN, temporary lockout | identity | `PinAccessScreen` + state |
| Link & consent, invalid code, consent required | carelink | `CareLinkScreen` + code-entry / awaiting-consent / confirmed / rejected / invalid-code states |
| Medications, details, add medication, create/manage treatment | treatment | treatment presentation package |
| Home, weekly schedule, voice confirmation, dose states | intake | intake presentation package |
| Alerts, alert detail, contact unavailable, note saved, attended | omission | omission presentation package |
| Family summary | monitoring | family monitoring presentation |
| History, insights, recommendations and empty states | analytics | analytics presentation package |
| Inventory, invalid quantity, restock registered | inventory | inventory presentation package |
| Accessibility and notification states | preferences | preferences presentation package |

Rule: one UX frame does not automatically mean one `Screen.kt`. Error/success/empty variants are normally modeled as UI state.

## Intake and dose outcomes

Figma file `jCppvxtSpLpHOrC3ZWUIVC`, Mobile Prototyping `563:2`. `DoseDetailScreen` renders the persisted detail; `DoseOutcomeScreen` renders a successful confirmation or a preserved omission. Pending, confirmed, late and omitted details use frames `563:3266`, `563:3370`, `563:3474` and `563:3578`. Confirmed, repeated, late and preserved-omission results use `563:291`, `563:2760`, `563:3770` and `563:3844`.

Success follows the server response. An idempotent replay preserves the original confirmation timestamp. A rejected confirmation reads the persisted intake before displaying an omission. The next-dose query uses the same older adult and excludes the current intake. When no next dose exists, the recorded detail labels its own scheduled time explicitly.

`VoiceConfirmationScreen` implements `563:242` and `563:2707` with native controls, recording permission, AAC/MP4 capture, validated results, retry and cancellation. The reinforced reminder `563:3682` appears for a pending intake after its scheduled time; the local notice does not claim push delivery.

## Agenda and adult navigation

`IntakeAgendaScreen` implements `563:500` with week/day selection, chronological intake data, persisted status, detail navigation, retry and empty states. The shared adult bar connects Inicio, Medicamentos, Agenda and Notas where the route supplies those callbacks. Device screenshots and interaction fixtures validate each variant independently.

## Inventory and replenishment

`InventoryScreen` models loading, initial stock, content, invalid quantity, replenishment success and request failures. Frames `563:1632`, `563:5213` and `563:5284` correspond to content, invalid quantity and replenished stock. Stock values and available estimates come from the backend contract. Cards, quantity fields, actions, stock ring and role navigation are native Compose controls; exported illustrations remain local assets.

## Accessibility and notifications

`AccessibilityScreen` covers text size, high contrast, reduced motion and reading assistance through preferences state. `NotificationPreferencesScreen` binds the settings supported by the preferences contract. Each saved/error variant has its own entry in the validation ledger; shared composition alone does not validate another variant.

## Identity, care links and family flows

Registration, verification, PIN access, caregiver profiles, care-link consent, subscription, family summary, alerts and personal notes remain in their respective bounded-context presentation packages. The app package composes dependencies and navigation. Clinical labels, medication names, schedules and progress use received data; illustration exports do not replace interactive controls.

## Validation evidence

The current per-frame implementation and visual acceptance records are in [the 70-variant ledger](figma-visual-validation.md). API behavior is documented in [backend contracts](../api/backend-contracts.md). Default resources are Spanish; explicit English resources support the EN prototype. Emulator fixtures contain isolated sample data. Public Render verification is independent of local tests and screenshots.
