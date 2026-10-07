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

- **No "Lote / nota" field.** The backend `Batch` has no `lot` yet (see `docs/api/backend-contracts.md`); the field is omitted until the contract adds it.
- **"≈ X días de tratamiento" shows "Sin estimación".** `daysRemaining` is not returned by the backend; it is an optional read-model field left `null`. No day estimate is computed in the app.
- **No bottom navigation bar.** The mockup shows a "Persona"-active tab bar; wiring the role-aware shell / `RoleAwareScaffold` is out of scope for this delivery. Navigation is back-stack based.
- **Single primary action.** The mockup has two buttons ("Registrar reposición" primary + "Guardar reposición" secondary); the screen collapses these into one primary "Guardar reposición" / "Save replenishment" button, the main action.
- **"Definir stock inicial" only on 404.** The initial-stock form (quantity + threshold) appears only in the `NotInitialized` state; there is no dedicated Figma frame for it, so it reuses the replenishment form components. No default threshold is assumed.
- **Blank medication unit.** The name comes from `TreatmentDetail` (`medicationLabelHint`); the unit ("comprimidos") is not available there, so counts render without a unit word ("5 restantes") until a medication read provides `presentation`.
- Not pixel-perfect: the screen uses `:shared` design-system components rather than reproducing the mockup as images.
