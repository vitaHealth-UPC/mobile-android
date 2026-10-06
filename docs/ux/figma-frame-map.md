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
