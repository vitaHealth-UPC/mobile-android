# Figma frame map

This file is the implementation traceability map between UX states, User Stories and code.

| UX area/state | Bounded Context | Planned screen/state |
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

## US-06 touch confirmation

Figma file `jCppvxtSpLpHOrC3ZWUIVC`, Mobile Prototyping `563:2`: pending detail `563:3266`, confirmed state `563:291`. `DoseDetailScreen` keeps pending/submitting/error/success in `DoseDetailUiState`. Success appears only after server confirmation. The prototype family-notified claim awaits a real notification contract. Voice frame `563:242` remains pending device integration.

## US-24 agenda

Weekly schedule frame `563:500` is implemented in `IntakeAgendaScreen` with local calendar week/day selection, real chronological API data, server outcome labels, detail navigation, retry and empty states. Calendar, timeline, tip and core tab assets were downloaded through the Figma MCP design workflow and stored locally; DM Serif Display is bundled with its OFL license.

This delivery wires the core Inicio/Agenda navigation. Notas/Más destinations, full prototype visual parity and emulator screenshot comparison remain pending; there is no configured Android emulator/device in the current environment. Static prototype drug names/dates are replaced by real resources. Do not count the whole prototype as completed because this screen builds.

## US-04 edit and deactivate a medication

Frames `563:1554` (Treatment Management), `563:2455` (Medication Updated), `563:2537` (Medication Deactivated), `563:2404` (required fields) and `563:3092` (Access Denied): `MedicationManagementScreen` (route `medication-management/{caregiverId}/{olderAdultId}/{olderAdultName}`). One card per medication with its Active / Inactive state, an inline edit form, a confirmation dialog before deactivating, and the "Protected history" note. The updated, deactivated and error banners are `MedicationManagementMessage` UI states; the restricted access card is `accessDenied`.

Not implemented on purpose: "Pause" is the treatment lifecycle of US-18 (`TreatmentLifecycleScreen`), "Reactivate" has no backend endpoint, and the 92% adherence chart belongs to `:analytics`.

Entry point: the "More" menu of the family summary offers "Medicamentos". This screen uses the colour constants that exist in `develop`; once the accessibility branches are merged it can switch to `tataTextColor()` / `tataMutedColor()` so it follows high contrast.
