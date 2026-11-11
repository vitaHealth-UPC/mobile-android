# Figma implementation mapping

This map connects Tata's native Android flows with the mobile prototype. The source Figma file is `jCppvxtSpLpHOrC3ZWUIVC`; its Mobile Prototyping section is `563:2`.

| User journey / visual state | Bounded context | Implementation |
| --- | --- | --- |
| Registration, verification and account access | Identity & Subscription | Registration, onboarding, password and PIN presentation |
| Linking code and adult consent | Care Link | Link acceptance and consent presentation |
| Medication and treatment operations | Treatment Management | Catalog, registration, regimen and lifecycle presentation |
| Home, agenda and scheduled dose | Intake Execution | Home, `IntakeAgendaScreen` and `DoseDetailScreen` |
| Touch and voice dose confirmation | Intake Execution | `DoseOutcomeScreen` and `VoiceConfirmationScreen` |
| Alerts, contact and notes | Family Monitoring | Alert list/detail and caregiver follow-up presentation |
| Adherence history and insights | Adherence Analytics | History, summary and recommendations presentation |
| Stock and replenishment | Inventory & Replenishment | `InventoryScreen` and stock forms |
| Accessibility and notifications | Accessibility & Preferences | Preference and notification screens |

## Intake and confirmation frames

| Frame | State |
| --- | --- |
| `563:242`, `563:2707` | Voice confirmation |
| `563:3266` | Scheduled dose detail |
| `563:3370` | Confirmed dose detail |
| `563:3474` | Late dose detail |
| `563:3578` | Omitted dose detail |
| `563:291`, `563:2760` | Confirmation results |
| `563:3770`, `563:3844` | Late/omission outcomes |
| `563:3682` | Reinforced reminder |
| `563:500` | Weekly/daily agenda |

## Inventory frames

`563:1632` represents the inventory view, `563:5213` the invalid-quantity state and `563:5284` the replenishment result. Stock figures are populated from the inventory API.

## Presentation conventions

A Figma frame represents a UI state, not necessarily an individual Kotlin file. Loading, validation, success, empty and error states are represented in screen state and Compose components. Data-bearing screens use mapped backend responses; interaction and navigation remain native Android components.

The dependency structure of these screens is described in [Mobile architecture](mobile-architecture.md).
