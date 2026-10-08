# Figma: 70 prototype frames

Source: VitaHealth `jCppvxtSpLpHOrC3ZWUIVC`, section `563:2`. Inventory obtained through Figma MCP. Each frame is a distinct visual and behavioral acceptance case, including error, success, empty and preference variants. Shared composables do not remove the obligation to reproduce every variant.

This combines a source-level contrast with the emulator visual audit below. Compared does not mean visually accepted. Shared implementations can cover several references, and partial compositions can still miss the target screen. No implementation percentage is inferred from Screen.kt counts.

| # | Frame | Figma node | Current code / gap | Visual acceptance |
| --- | --- | --- | --- | --- |
| 1 | Landing - Value & Features | 563:1849 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 2 | Landing - Plans & Contact | 563:1913 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 3 | Onboarding | 563:5 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 4 | Caregiver Registration | 563:1398 | CaregiverRegistrationScreen: Native fields, progress, editable six-digit verification and duplicate/expired banners restyled from MCP | Compared in emulator: `registration-content`; differences recorded below; not fully accepted |
| 5 | Link & Consent | 563:1440 | CareLinkScreen: Screen and invalid-code state exist | Compared in emulator: `link-code`; differences recorded below; not fully accepted |
| 6 | PIN Access | 563:1338 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Compared in emulator: `pin-access`; differences recorded below; not fully accepted |
| 7 | Login | 641:16 | SessionAccessScreen: Rebuilt from MCP; password login works; social/recovery options need integrations | Compared in emulator: `prior installed-app login pass`; differences recorded below; not fully accepted |
| 8 | Home | 563:58 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Compared in emulator: `home-existing-dose`; differences recorded below; not fully accepted |
| 9 | My Medications | 563:361 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 10 | Medication Detail | 563:142 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-pending`; differences recorded below; not fully accepted |
| 11 | Weekly Schedule | 563:500 | IntakeAgendaScreen: Agenda and outcome labels exist | Compared in emulator: `agenda-existing`; differences recorded below; not fully accepted |
| 12 | Voice Confirmation | 563:242 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 13 | Dose Confirmed | 563:291 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-success`; differences recorded below; not fully accepted |
| 14 | Accessibility | 563:618 | AccessibilityScreen: Five native preference controls restyled from MCP; exact vector icons and help dialog. Dark theme and adult tab bar remain missing | Compared in emulator: `accessibility-content`; differences recorded below; not fully accepted |
| 15 | Family Summary | 563:727 | FamilySummaryScreen: Summary exists; consent-required presentation needs comparison | Compared in emulator: `family-summary`; differences recorded below; not fully accepted |
| 16 | Linked Person | 563:1121 | CaregiverProfilesScreen: Partial: linked-profile selection; not the full linked-person mockup | Compared in emulator: `linked-person`; differences recorded below; not fully accepted |
| 17 | Alerts | 563:819 | AlertsScreen: Dedicated read-only list added in feature/us-27-alert-detail and merged through PR 38 | Compared in emulator: `alerts-list`; differences recorded below; not fully accepted |
| 18 | Alert Detail | 563:918 | AlertDetailScreen: Dedicated read-only detail added in feature/us-27-alert-detail; follow-up actions absent | Compared in emulator: `alert-detail`; differences recorded below; not fully accepted |
| 19 | History & Insights | 563:1017 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Compared in emulator: `history-content`; differences recorded below; not fully accepted |
| 20 | Adherence Recommendations | 563:1767 | AdherenceRecommendationsScreen: Screen and evidence states exist | Compared in emulator: `recommendations-content`; differences recorded below; not fully accepted |
| 21 | Notes - Adult | 576:2785 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 22 | Add Medication - Caregiver | 563:453 | MedicationRegistrationScreen: Form and validation exist | Compared in emulator: `medication-registration`; differences recorded below; not fully accepted |
| 23 | Create Treatment | 563:1490 | TreatmentCreationScreen + regimen steps: Split into steps; full single-frame form comparison needed | Compared in emulator: `treatment-create`; differences recorded below; not fully accepted |
| 24 | Treatment Management | 563:1554 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Compared in emulator: `medication-management; treatment-list; treatment-paused`; differences recorded below; not fully accepted |
| 25 | Inventory & Restock | 563:1632 | InventoryScreen: Native stock ring, gradients, replenishment actions/fields and last batch restyled from MCP; caregiver bottom tabs remain missing | Compared in emulator: `inventory-content`; differences recorded below; not fully accepted |
| 26 | Notification Preferences | 563:1219 | NotificationPreferencesScreen: MCP comparison confirms missing medication cadence, caregiver alert thresholds/daily summary, emergency contacts/escalation and CALL channel; only quiet hours and PUSH/SMS/EMAIL implemented | Compared in emulator: `notifications-content`; differences recorded below; not fully accepted |
| 27 | Notes - Caregiver | 576:2859 | FamilySummaryScreen notes dialog: Partial: read-only notes; editor missing | Figma inspected; no matching renderable state: Read-only notes dialog does not reproduce this dedicated notes/editor screen |
| 28 | Plan & Subscription | 563:1701 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 29 | PIN Setup | 563:1970 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Compared in emulator: `pin-setup`; differences recorded below; not fully accepted |
| 30 | PIN Incorrect | 563:2034 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Compared in emulator: `pin-incorrect`; differences recorded below; not fully accepted |
| 31 | PIN Temporarily Blocked | 563:2098 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Compared in emulator: `pin-blocked`; differences recorded below; not fully accepted |
| 32 | Duplicate Email | 563:2162 | CaregiverRegistrationScreen: Native fields, progress, editable six-digit verification and duplicate/expired banners restyled from MCP | Compared in emulator: `registration-duplicate`; differences recorded below; not fully accepted |
| 33 | Verification Expired | 563:2208 | CaregiverRegistrationScreen: Native fields, progress, editable six-digit verification and duplicate/expired banners restyled from MCP | Compared in emulator: `registration-expired`; differences recorded below; not fully accepted |
| 34 | Invalid Link Code | 563:2254 | CareLinkScreen: Screen and invalid-code state exist | Compared in emulator: `link-invalid`; differences recorded below; not fully accepted |
| 35 | Consent Required | 563:2308 | FamilySummaryScreen: Summary exists; consent-required presentation needs comparison | Compared in emulator: `family-consent-required`; differences recorded below; not fully accepted |
| 36 | Medication Required Fields Error | 563:2404 | MedicationRegistrationScreen: Form and validation exist | Compared in emulator: `medication-registration-error`; differences recorded below; not fully accepted |
| 37 | Medication Updated | 563:2455 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Compared in emulator: `medication-updated`; differences recorded below; not fully accepted |
| 38 | Medication Deactivated | 563:2537 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Compared in emulator: `medication-deactivated`; differences recorded below; not fully accepted |
| 39 | Medication Reminder Due | 563:2619 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Figma inspected; no matching renderable state: No in-app reminder-due visual state; an OS notification is not this frame |
| 40 | Voice Not Recognized | 563:2707 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 41 | Dose Already Confirmed | 563:2760 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Figma inspected; no matching renderable state: No dedicated already-confirmed outcome; generic confirmation failure text is not this frame |
| 42 | No Adherence Data | 563:2834 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Compared in emulator: `history-insufficient`; differences recorded below; not fully accepted |
| 43 | Treatment Incomplete | 563:2942 | TreatmentCreationScreen + regimen steps: Split into steps; full single-frame form comparison needed | Compared in emulator: `treatment-incomplete`; differences recorded below; not fully accepted |
| 44 | Treatment Paused | 563:3010 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Compared in emulator: `treatment-paused`; differences recorded below; not fully accepted |
| 45 | Treatment Access Denied | 563:3092 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Compared in emulator: `treatment-access-denied`; differences recorded below; not fully accepted |
| 46 | No Next Dose | 563:3178 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Compared in emulator: `home-existing-empty`; differences recorded below; not fully accepted |
| 47 | Dose Detail - Pending | 563:3266 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-pending`; differences recorded below; not fully accepted |
| 48 | Dose Detail - Confirmed | 563:3370 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-confirmed`; differences recorded below; not fully accepted |
| 49 | Dose Detail - Late | 563:3474 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-late`; differences recorded below; not fully accepted |
| 50 | Dose Detail - Omitted | 563:3578 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-omitted`; differences recorded below; not fully accepted |
| 51 | Reinforced Reminder | 563:3682 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Figma inspected; no matching renderable state: No in-app reinforced-reminder visual state |
| 52 | Late Dose Confirmed | 563:3770 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-late-success`; differences recorded below; not fully accepted |
| 53 | Omission Preserved | 563:3844 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Compared in emulator: `dose-existing-omission-preserved`; differences recorded below; not fully accepted |
| 54 | Agenda With Statuses | 563:3918 | IntakeAgendaScreen: Agenda and outcome labels exist | Compared in emulator: `agenda-existing-statuses`; differences recorded below; not fully accepted |
| 55 | Empty Intake History | 563:4037 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Compared in emulator: `history-empty`; differences recorded below; not fully accepted |
| 56 | Contact Unavailable | 563:4145 | FamilySummaryScreen dialogs: Partial: alerts/contact dialogs; dedicated Figma screens missing | Figma inspected; no matching renderable state: Generic contact failure dialog lacks the dedicated disabled-contact follow-up presentation |
| 57 | Follow-up Note Saved | 563:4245 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 58 | Alert Attended | 563:4345 | AlertDetailScreen: Can display server ATTENDED status; the mark-as-attended action is not implemented | Compared in emulator: `alert-attended-read-only`; differences recorded below; not fully accepted |
| 59 | Adherence Period Changed | 563:4445 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Compared in emulator: `history-period-changed`; differences recorded below; not fully accepted |
| 60 | Insufficient Evidence | 563:4550 | AdherenceRecommendationsScreen: Screen and evidence states exist | Compared in emulator: `recommendations-insufficient`; differences recorded below; not fully accepted |
| 61 | Large Text Enabled | 563:4638 | AccessibilityScreen: Five native preference controls restyled from MCP; exact vector icons and help dialog. Dark theme and adult tab bar remain missing | Compared in emulator: `accessibility-large-text`; differences recorded below; not fully accepted |
| 62 | High Contrast Enabled | 563:4751 | AccessibilityScreen: Five native preference controls restyled from MCP; exact vector icons and help dialog. Dark theme and adult tab bar remain missing | Compared in emulator: `accessibility-high-contrast`; differences recorded below; not fully accepted |
| 63 | Reduced Motion Enabled | 563:4864 | AccessibilityScreen: Five native preference controls restyled from MCP; exact vector icons and help dialog. Dark theme and adult tab bar remain missing | Compared in emulator: `accessibility-reduced-motion`; differences recorded below; not fully accepted |
| 64 | Reading Assistance Enabled | 563:4977 | AccessibilityScreen: Five native preference controls restyled from MCP; exact vector icons and help dialog. Dark theme and adult tab bar remain missing | Compared in emulator: `accessibility-reading-assistance`; differences recorded below; not fully accepted |
| 65 | Notification Preferences Saved | 563:5090 | NotificationPreferencesScreen: MCP comparison confirms missing medication cadence, caregiver alert thresholds/daily summary, emergency contacts/escalation and CALL channel; only quiet hours and PUSH/SMS/EMAIL implemented | Compared in emulator: `notifications-saved`; differences recorded below; not fully accepted |
| 66 | Invalid Inventory Quantity | 563:5213 | InventoryScreen: Native stock ring, gradients, replenishment actions/fields and last batch restyled from MCP; caregiver bottom tabs remain missing | Compared in emulator: `inventory-invalid`; differences recorded below; not fully accepted |
| 67 | Stock Replenished | 563:5284 | InventoryScreen: Native stock ring, gradients, replenishment actions/fields and last batch restyled from MCP; caregiver bottom tabs remain missing | Compared in emulator: `inventory-replenished`; differences recorded below; not fully accepted |
| 68 | Subscription Updated | 563:5354 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 69 | External Destination Unavailable | 563:5425 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |
| 70 | Internationalization | 665:183 | Dedicated screen/state not implemented | Figma inspected; dedicated screen/state absent; no runtime comparison possible |

## Asset resolution

Exact link avatar export: 232×232 at 4× for a 58 dp slot. PIN avatar: 248×248 for 62 dp; glow: 1384×1384 from the original SVG, rendered at its 346×346 dp effect bounds. Preserve those geometries instead of stretching assets. PIN keys are native Compose circles with shadows, text and click handlers. The decorative glow is rasterized from the original SVG at 4× with transparency; the initial opaque export was discarded after emulator comparison. Buttons, cards, inputs and navigation are native Compose components. Source photo in Figma is 96×96: exporting cannot restore missing photographic detail. SVG social icons retain vector resolution. Whole-screen Figma screenshots are references, never application assets.


## Runtime target

Android defaults to `https://web-services-yzxl.onrender.com/`. The published OpenAPI exposes 64 operations and retains the session, PIN and care-link paths used by these screens. Override `TATA_API_BASE_URL` when testing a local server. Existing repository tests validate authentication requests and error mappings; a signed-in production account was not used for this visual pass. PIN variants also have dedicated Compose previews for frames 06, 29, 30 and 31.

## Accessibility visual pass

Compared frame 14 and variants 61–64 through MCP. The five existing settings retain their command handlers and persistence. Restyled native switches, row dimensions, type, separators, SVG icon slots (all 24×24), help banner and responsive header. This is partial fidelity: dark theme, the adult bottom navigation and complete variant positioning remain open; do not count these five frames as fully accepted. Notification frames 26/65 were compared at source level and their missing categories are listed above.

## Registration visual pass

Compared frame 4 plus duplicate-email/expired-verification variants 32–33 using MCP. Native screen was rendered in an emulator instrumentation host with production insets and compared visually. Interaction test verifies disabled pre-registration verification, six-digit filtering, verification callback, expired-code recovery and duplicate-email banner. Existing identity unit tests pass. Fixtures are isolated UI test data: no production account was created. Fields lock after account creation and the pre-registration instructions reflect that a code has not yet been sent. Error variants have functional assertions; they are not claimed as fully screenshot-accepted.

## Inventory visual pass

Compared frame 25 through MCP. Stock status ring is a native Compose shape and its displayed coverage comes from API daysRemaining; absent coverage shows a dash rather than a fabricated estimate. Registrar reposición focuses the numeric field; Guardar reposición retains the real save callback and busy state. Last batch date, quantity and lot come from the read model. Original medication icon is local SVG at its 24×24 design slot. Bottom caregiver tabs are still absent; defining stock initial is restricted to the not-initialized state and is not offered as an overwrite of existing inventory. Do not mark the entire frame fully accepted.

## Emulator audit — 2026-10-07

All 70 Figma reference frames were retrieved through MCP and inspected. The ledger now maps 53 reference cases to emulator screenshots of their corresponding implementation, including the prior login comparison. Five previously counted cases have no matching renderable state (27, 39, 41, 51, 56); twelve have no dedicated implementation (1, 2, 3, 9, 12, 21, 28, 40, 57, 68, 69, 70). The newly merged read-only attended-alert presentation is included as a partial implementation. These are audit dispositions, not 53 accepted screens or a completed-screen percentage.

The app harness renders production composables with isolated test fixtures, Spanish es-419 resources, native safe-drawing insets and the real accessibility theme. It does not create production accounts, make authenticated requests or verify every navigation route end to end. Data such as dates, stock, medication names and outcomes remain dynamic in production. Earlier blank shell screenshots were discarded and replaced by Compose root captures or recaptured settled native frames. Tests passing establish rendering/interactions; visual acceptance remains a separate manual comparison.

| Cases | Observed differences from Figma |
| --- | --- |
| 4, 32, 33 | General form composition matches much better; pre-account verification is intentionally disabled. Verification and error banners still differ in placement and detail. |
| 5, 34 | Native structure is close. Invalid-code feedback is bare text rather than the Figma error banner. Profile content requires actual loaded adult data. |
| 6, 29–31 | Native keypad, avatar and main structure retained. Extra back action and banner spacing differ; dots correctly reflect entered digits rather than permanent demo bullets. |
| 7 | Prior login comparison retained; OAuth and recovery options do not establish working integrations. |
| 8, 46 | Missing daily progress, avatar, medication illustration, voice/all-intakes shortcuts and adult tab bar. Header, card spacing and empty composition differ. Empty state must not fabricate the mockup's example dose. |
| 10, 47–50 | Missing illustration, category/purpose field, information icons, preparation illustration and adult tabs. Oversized cards and a sans-serif title differ; pending confirmation falls below the initial viewport. Statuses are correctly distinct. |
| 11, 54 | Four status styles render, but oversized rows push the fourth intake below the viewport. Timeline, tip and five-option adult tab bar differ; white raster icons have poor contrast. |
| 13, 52, 53 | Success is a simple card rather than the complete outcome composition. Late success still says ordinary confirmation; omission stays in detail. Missing next-dose/family-notification panels and adult tabs. |
| 14, 61–64 | Native preferences and real large-text/contrast/reading/motion theme states render. Dark-theme row and adult tab bar are absent; message copy and spacing differ. |
| 15, 35 | Similar summary sections, but the avatar is an initial and can be clipped by oversized nearby artwork. Raster icons are blurry. Consent denial uses a generic retry card instead of the centered restricted-follow-up state. |
| 16 | Linked-adult list renders, but it is not the full profile with age, adherence, medication summary, contact shortcuts and caregiver notes. |
| 17, 18, 58 | Dedicated read-only alerts now render. Missing reference header/summary presentation, upcoming/completed semantics, adult profile, reminder timeline and follow-up actions. Displaying ATTENDED is not proof that the attend command works. |
| 19, 42, 55, 59 | Metrics, graph, pattern, recent intakes and period/empty states render with fixture data. Serif title, card/graph geometry, shadows, empty-panel position and period-change feedback differ. Recent rows need scrolling in the current layout. |
| 20, 60 | Data-driven heat map and recommendations render. Serif title, density, card geometry, button anchoring and insufficient-evidence positioning differ. Recommendation count and matrix dimensions depend on supplied data. |
| 22, 36 | Native form exists. Missing back arrow and frequency dropdown; default floating labels differ from in-card labels. Medication artwork is visibly blurry. Error copy/spacing differ. |
| 23, 43 | Existing creation is a draft wizard, not the assembled treatment review in Figma. Missing same-screen regimen/reminder summary and agenda preview; incomplete-state presentation differs. |
| 24, 37, 38, 44, 45 | Medication management, treatment list and lifecycle are separate compositions. Missing profile/adherence panels, combined edit/pause actions and caregiver tabs. Access-denied layout differs. |
| 25, 66, 67 | Native stock ring, numeric input, lot, last batch and error/success feedback render. Caregiver tabs are absent; error location and success composition differ. Last-batch date still follows device-default formatting under a Spanish UI context. Initial-stock action is correctly restricted to uninitialized inventory. |
| 26, 65 | Quiet hours and push/SMS/email render in Spanish. Medication cadence, caregiver thresholds/summary, emergency contacts/escalation, call channel and caregiver tabs are missing. |

Reproduce with `gradlew.bat :app:connectedDebugAndroidTest :intake:connectedDebugAndroidTest` on an available emulator. App captures are exported by the test to device `Pictures/TataAudit`; intake captures use device `/data/local/tmp`. Figma screenshots are comparison references, never app assets. The full app suite passed 16 instrumentation cases; intake passed 3 interaction/render cases. Monitoring and app unit tests passed while integrating US-27 with current develop. Focused fixture reruns cover period, link-profile and quiet-hours comparison settings.

## Implementation consistency

Preserve straightforward Compose structures (`Column`, `Row`, `Text`, native fields, switches and buttons) across feature modules. Extract only small repeated components and shared typography/color/spacing tokens; avoid adding an abstraction layer just to reproduce a mockup. Production names, dates, doses, stock, percentages and statuses must come from read models. Keep static interface labels in resources, reuse existing callbacks/view models, and place example data only in previews/tests. Export only genuine artwork/photos/icons, using original vectors or adequate-resolution transparent assets; do not replace interactive screens with screenshots.
