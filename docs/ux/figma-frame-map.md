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

## US-40 to US-43 inventory

Figma file `jCppvxtSpLpHOrC3ZWUIVC`, "Inventory & Restock" screens. The three mockups are states of one screen, `InventoryScreen`, driven by `InventoryUiState`:

| Figma frame | State shown | `InventoryUiState` |
| --- | --- | --- |
| `236:197` (Phone/25) | Low stock ("Stock bajo") | `Ready` with `stock.status = LOW` |
| `294:3328` (Phone/67) | Stock available / replenished | `Ready` with `stock.status = AVAILABLE` |
| `294:3255` (Phone/66) | Invalid replenishment quantity | `Ready` with `replenishmentErrorCode = INVALID_QUANTITY` |

States not drawn as separate frames but modeled in code: `Loading` (initial `GET`), `NotInitialized` (HTTP `404`, US-40 initial-stock form), and `Error` (network/other, with retry). Low-stock evaluation comes from the backend `lowStock` flag (`StockStatus`), not a visual constant.

Entry point: there is no dedicated inventory tab in this delivery. Inventory is reached from `TreatmentDetailScreen` via an "Inventario" / "Inventory" button (shown only when the treatment has a `medicationId`), composed in `:app` through `RootDestination.Inventory`.

### Divergences from the mockup

- **"Lote / nota"** accepts optional batch metadata and displays it in the last replenishment card.
- **"≈ X días de tratamiento" shows "Sin estimación".** `daysRemaining` is not returned by the backend; it is an optional read-model field left `null`. No day estimate is computed in the app.
- **No bottom navigation bar.** The mockup shows a "Persona"-active tab bar; wiring the role-aware shell / `RoleAwareScaffold` is out of scope for this delivery. Navigation is back-stack based.
- **Single primary action.** The mockup has two buttons ("Registrar reposición" primary + "Guardar reposición" secondary); the screen collapses these into one primary "Guardar reposición" / "Save replenishment" button, the main action.
- **"Definir stock inicial" only on 404.** The initial-stock form (quantity + threshold) appears only in the `NotInitialized` state; there is no dedicated Figma frame for it, so it reuses the replenishment form components. No default threshold is assumed.
- **Blank medication unit.** The name comes from `TreatmentDetail` (`medicationLabelHint`); the unit ("comprimidos") is not available there, so counts render without a unit word ("5 restantes") until a medication read provides `presentation`.
- Not pixel-perfect: the screen uses `:shared` design-system components rather than reproducing the mockup as images.
## US-35 text size

Figma file `jCppvxtSpLpHOrC3ZWUIVC`, Mobile Prototyping `563:2`: Accessibility `563:618`, Large Text Enabled `563:4638`. `AccessibilityScreen` (route `accessibility/{userId}`) keeps the saved / saved-offline / error banner in `AccessibilityUiState.message`. Only the "Large text" row is implemented here; the other rows arrive with US-36 to US-38.

Not implemented on purpose: the "Dark theme" and "Language" rows and the "Need help?" card of the frame have no User Story and no backend field. The row icons are text glyphs until the Figma vector assets are exported.

Entry point: the "More" menu of the family summary (`FamilySummaryScreen`) now offers "Accesibilidad". The older-adult shell has no "More" tab yet.

## US-36 high contrast

Frame `563:4751` (High Contrast Enabled) is the same `AccessibilityScreen` with the contrast row on: the banner message is the `HighContrastSaved` UI state. `TataTheme` swaps to a high contrast colour scheme and `TataCard` draws a border; `tataTextColor()` and `tataMutedColor()` give the stronger text colours.

Screens of other modules that still use the `TataText` / `TataMuted` constants directly keep their normal colours until they switch to those two functions.

## US-37 reduced motion (and the voice confirmation setting)

Frame `563:4864` (Reduced Motion Enabled) is the "Interaction" section of `AccessibilityScreen`; its saved banner is the `ReducedMotionSaved` UI state. With the setting on, `TataNavHost` replaces the screen transitions by `EnterTransition.None` / `ExitTransition.None`. Other animations must read `LocalTataAccessibility.current.reducedMotion` when they are added.

The "Voice confirmation" row of the same section only stores the preference (`voiceConfirmationEnabled` in the backend). The voice flow itself belongs to US-06 in `:intake`.

## US-38 reading assistance

Frame `563:4977` (Reading Assistance Enabled) is the "Help" section of `AccessibilityScreen`. The setting has real behaviour: `TataTheme` builds the Material typography through `tataTypography(readingAssistance)`, which lifts every text style to at least Medium weight and raises the line height to 1.6 times the font size, with slightly more letter spacing. Font sizes do not change (that is the text size setting).

Text that sets its own `fontSize` / `lineHeight` / `fontWeight` directly does not follow this setting; screens should use the Material text styles.

## US-39 quiet hours and channels (US-28 only in part)

Frame `563:1219` (Notification Preferences) and `563:5090` (Preferences Saved): `NotificationPreferencesScreen` (route `notification-preferences/{userId}`) holds the "Quiet hours" card and the "Notification channels" card; saved / error banners are `NotificationMessage` UI states. Quiet hours start from 10:00 p. m. - 7:00 a. m. when turned on and the times are picked with the system time dialog. Every change is sent at once, because the backend replaces quiet hours and channels together.

Not implemented on purpose, because the backend has no field or endpoint for them (US-28 asks for notification categories): the "Medication reminders" cadence card, the "Caregiver alerts" card with its delays and daily summary, the "Emergency escalation" card, and the "Calls" channel. The backend only stores quiet hours and the channels PUSH, SMS and EMAIL.

Entry point: the "More" menu of the family summary offers "Preferencias de notificación".
## US-04 edit and deactivate a medication

Frames `563:1554` (Treatment Management), `563:2455` (Medication Updated), `563:2537` (Medication Deactivated), `563:2404` (required fields) and `563:3092` (Access Denied): `MedicationManagementScreen` (route `medication-management/{caregiverId}/{olderAdultId}/{olderAdultName}`). One card per medication with its Active / Inactive state, an inline edit form, a confirmation dialog before deactivating, and the "Protected history" note. The updated, deactivated and error banners are `MedicationManagementMessage` UI states; the restricted access card is `accessDenied`.

Not implemented on purpose: "Pause" is the treatment lifecycle of US-18 (`TreatmentLifecycleScreen`), "Reactivate" has no backend endpoint, and the 92% adherence chart belongs to `:analytics`.

Entry point: the "More" menu of the family summary offers "Medicamentos". This screen uses the colour constants that exist in `develop`; once the accessibility branches are merged it can switch to `tataTextColor()` / `tataMutedColor()` so it follows high contrast.

## US-01 session entry and PIN

Prototype `641:18` supplies the password entry form; `563:1340` supplies the PIN keypad. Setup, incorrect and blocked PINs are states of `PinAccessScreen`. The screens use the existing Compose form, button, serif and accessibility tokens and call the live session API.

## US-12 profile entry and care-link handoff

`CaregiverProfilesScreen` presents real confirmed links, an empty state, profile registration and the temporary code. It follows the person identity hierarchy and shared card/form tokens consulted in prototype `563:1123`, using actual names and avoiding the prototype's sample adherence figures. Date selection uses the accessible Android date picker. Existing link/consent states receive the generated code; the adult reviews and grants consent explicitly.

## Onboarding (identity)

Frame `563:5` (Onboarding): `OnboardingScreen`, the first screen of a fresh install. "Comenzar" opens the caregiver registration and "Iniciar sesión" opens the sign-in; both remember on the device that the welcome was seen, so the next launches open on the sign-in. The sunlight, heart and leaf decorations and the butterfly are the Figma assets; the "Tata" wordmark is drawn with the system cursive font (Dancing Script on Android), because the wordmark layer cannot be exported on its own.

The three reminder cards are decoration with sample medications and are hidden from TalkBack. Their position follows the 393 dp prototype frame, scaled to the real width. The glow washes are approximated with gradients instead of the blurred ellipses.
