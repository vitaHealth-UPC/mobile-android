# Bounded Context map

The Android application uses the same canonical domain terminology as the EventStorming/C4 work.

| Package | Bounded Context | Mobile responsibility |
| --- | --- | --- |
| `identity` | Identity & Subscription | onboarding, registration, email verification, PIN, subscription |
| `carelink` | Care Link | caregiver/adult link, consent, linked person |
| `treatment` | Treatment Management | medications, treatment creation and treatment lifecycle |
| `intake` | Intake Execution | home, schedule, dose confirmation, voice flow, dose states |
| `omission` | Omission & Escalation | backend-owned omission/escalation; Android FCM device registration, session-authorized push delivery, native notifications and tap routing (no Compose screens) |
| `monitoring` | Family Monitoring | caregiver summary, alert list/detail/status, follow-up notes and monitoring views |
| `analytics` | Adherence Analytics | history, adherence metrics, recommendations and empty states |
| `inventory` | Inventory & Replenishment | stock and replenishment |
| `preferences` | Accessibility & Preferences | accessibility and notification preferences |

All contexts live in `app/src/main/kotlin/com/vitahealth/tata`. Domain rules remain in their context; navigation and cross-context composition belong to the `app` package.
Caregiver-facing alert state and follow-up belong to Family Monitoring
(backend TS09). Omission generation/escalation and push distribution
belong to Omission & Escalation (backend TS05/TS06). This boundary
does not authorize direct private-package imports between contexts.
