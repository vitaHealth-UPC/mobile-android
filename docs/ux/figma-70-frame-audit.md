# Figma: 70 prototype frames

Source: VitaHealth `jCppvxtSpLpHOrC3ZWUIVC`, section `563:2`. Inventory obtained through Figma MCP. Each frame is a distinct visual and behavioral acceptance case, including error, success, empty and preference variants. Shared composables do not remove the obligation to reproduce every variant.

This is a source-level contrast, not a claim that every listed state has been tested. Visual acceptance requires installed-app screenshots and checking every visible option. No percentage is inferred from Screen.kt counts.

| # | Frame | Figma node | Current code / gap | Visual acceptance |
| --- | --- | --- | --- | --- |
| 1 | Landing - Value & Features | 563:1849 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 2 | Landing - Plans & Contact | 563:1913 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 3 | Onboarding | 563:5 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 4 | Caregiver Registration | 563:1398 | CaregiverRegistrationScreen: Screen and account/verification states exist | Not yet validated against installed app |
| 5 | Link & Consent | 563:1440 | CareLinkScreen: Screen and invalid-code state exist | Not yet validated against installed app |
| 6 | PIN Access | 563:1338 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Not yet validated against installed app |
| 7 | Login | 641:16 | SessionAccessScreen: Rebuilt from MCP; password login works; social/recovery options need integrations | Not yet validated against installed app |
| 8 | Home | 563:58 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Not yet validated against installed app |
| 9 | My Medications | 563:361 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 10 | Medication Detail | 563:142 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 11 | Weekly Schedule | 563:500 | IntakeAgendaScreen: Agenda and outcome labels exist | Not yet validated against installed app |
| 12 | Voice Confirmation | 563:242 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 13 | Dose Confirmed | 563:291 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 14 | Accessibility | 563:618 | AccessibilityScreen: Preference controls exist; compare each enabled variant | Not yet validated against installed app |
| 15 | Family Summary | 563:727 | FamilySummaryScreen: Summary exists; consent-required presentation needs comparison | Not yet validated against installed app |
| 16 | Linked Person | 563:1121 | CaregiverProfilesScreen: Partial: linked-profile selection; not the full linked-person mockup | Not yet validated against installed app |
| 17 | Alerts | 563:819 | FamilySummaryScreen dialogs: Partial: alerts/contact dialogs; dedicated Figma screens missing | Not yet validated against installed app |
| 18 | Alert Detail | 563:918 | FamilySummaryScreen dialogs: Partial: alerts/contact dialogs; dedicated Figma screens missing | Not yet validated against installed app |
| 19 | History & Insights | 563:1017 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Not yet validated against installed app |
| 20 | Adherence Recommendations | 563:1767 | AdherenceRecommendationsScreen: Screen and evidence states exist | Not yet validated against installed app |
| 21 | Notes - Adult | 576:2785 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 22 | Add Medication - Caregiver | 563:453 | MedicationRegistrationScreen: Form and validation exist | Not yet validated against installed app |
| 23 | Create Treatment | 563:1490 | TreatmentCreationScreen + regimen steps: Split into steps; full single-frame form comparison needed | Not yet validated against installed app |
| 24 | Treatment Management | 563:1554 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Not yet validated against installed app |
| 25 | Inventory & Restock | 563:1632 | InventoryScreen: Inventory/replenishment and validation states exist | Not yet validated against installed app |
| 26 | Notification Preferences | 563:1219 | NotificationPreferencesScreen: Partial: channels/quiet hours; compare all categories | Not yet validated against installed app |
| 27 | Notes - Caregiver | 576:2859 | FamilySummaryScreen notes dialog: Partial: read-only notes; editor missing | Not yet validated against installed app |
| 28 | Plan & Subscription | 563:1701 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 29 | PIN Setup | 563:1970 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Not yet validated against installed app |
| 30 | PIN Incorrect | 563:2034 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Not yet validated against installed app |
| 31 | PIN Temporarily Blocked | 563:2098 | PinAccessScreen: Rebuilt from MCP; setup/incorrect/locked states | Not yet validated against installed app |
| 32 | Duplicate Email | 563:2162 | CaregiverRegistrationScreen: Screen and account/verification states exist | Not yet validated against installed app |
| 33 | Verification Expired | 563:2208 | CaregiverRegistrationScreen: Screen and account/verification states exist | Not yet validated against installed app |
| 34 | Invalid Link Code | 563:2254 | CareLinkScreen: Screen and invalid-code state exist | Not yet validated against installed app |
| 35 | Consent Required | 563:2308 | FamilySummaryScreen: Summary exists; consent-required presentation needs comparison | Not yet validated against installed app |
| 36 | Medication Required Fields Error | 563:2404 | MedicationRegistrationScreen: Form and validation exist | Not yet validated against installed app |
| 37 | Medication Updated | 563:2455 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Not yet validated against installed app |
| 38 | Medication Deactivated | 563:2537 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Not yet validated against installed app |
| 39 | Medication Reminder Due | 563:2619 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Not yet validated against installed app |
| 40 | Voice Not Recognized | 563:2707 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 41 | Dose Already Confirmed | 563:2760 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 42 | No Adherence Data | 563:2834 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Not yet validated against installed app |
| 43 | Treatment Incomplete | 563:2942 | TreatmentCreationScreen + regimen steps: Split into steps; full single-frame form comparison needed | Not yet validated against installed app |
| 44 | Treatment Paused | 563:3010 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Not yet validated against installed app |
| 45 | Treatment Access Denied | 563:3092 | TreatmentListScreen / MedicationManagementScreen / TreatmentLifecycleScreen: Partial composition across separate screens | Not yet validated against installed app |
| 46 | No Next Dose | 563:3178 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Not yet validated against installed app |
| 47 | Dose Detail - Pending | 563:3266 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 48 | Dose Detail - Confirmed | 563:3370 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 49 | Dose Detail - Late | 563:3474 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 50 | Dose Detail - Omitted | 563:3578 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 51 | Reinforced Reminder | 563:3682 | NextDoseHomeScreen: Screen exists; reminder variants require full comparison | Not yet validated against installed app |
| 52 | Late Dose Confirmed | 563:3770 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 53 | Omission Preserved | 563:3844 | DoseDetailScreen: Detail/outcome states exist; compare each variant | Not yet validated against installed app |
| 54 | Agenda With Statuses | 563:3918 | IntakeAgendaScreen: Agenda and outcome labels exist | Not yet validated against installed app |
| 55 | Empty Intake History | 563:4037 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Not yet validated against installed app |
| 56 | Contact Unavailable | 563:4145 | FamilySummaryScreen dialogs: Partial: alerts/contact dialogs; dedicated Figma screens missing | Not yet validated against installed app |
| 57 | Follow-up Note Saved | 563:4245 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 58 | Alert Attended | 563:4345 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 59 | Adherence Period Changed | 563:4445 | AdherenceHistoryScreen: Screen, period controls and empty states exist | Not yet validated against installed app |
| 60 | Insufficient Evidence | 563:4550 | AdherenceRecommendationsScreen: Screen and evidence states exist | Not yet validated against installed app |
| 61 | Large Text Enabled | 563:4638 | AccessibilityScreen: Preference controls exist; compare each enabled variant | Not yet validated against installed app |
| 62 | High Contrast Enabled | 563:4751 | AccessibilityScreen: Preference controls exist; compare each enabled variant | Not yet validated against installed app |
| 63 | Reduced Motion Enabled | 563:4864 | AccessibilityScreen: Preference controls exist; compare each enabled variant | Not yet validated against installed app |
| 64 | Reading Assistance Enabled | 563:4977 | AccessibilityScreen: Preference controls exist; compare each enabled variant | Not yet validated against installed app |
| 65 | Notification Preferences Saved | 563:5090 | NotificationPreferencesScreen: Partial: channels/quiet hours; compare all categories | Not yet validated against installed app |
| 66 | Invalid Inventory Quantity | 563:5213 | InventoryScreen: Inventory/replenishment and validation states exist | Not yet validated against installed app |
| 67 | Stock Replenished | 563:5284 | InventoryScreen: Inventory/replenishment and validation states exist | Not yet validated against installed app |
| 68 | Subscription Updated | 563:5354 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 69 | External Destination Unavailable | 563:5425 | Dedicated screen/state not implemented | Not yet validated against installed app |
| 70 | Internationalization | 665:183 | Dedicated screen/state not implemented | Not yet validated against installed app |

## Asset resolution

Exact link avatar export: 232×232 at 4× for a 58 dp slot. PIN avatar: 248×248 for 62 dp; key: 360×360 including effect bounds for a 54 dp key; glow: 1204×1384 including clipped effect bounds, rendered at 301×346 dp. Preserve those geometries instead of stretching assets. Glow is stored as lossless WebP with unchanged pixels. Source photo in Figma is 96×96: exporting cannot restore missing photographic detail. SVG social icons retain vector resolution. Whole-screen Figma screenshots are references, never application assets.

