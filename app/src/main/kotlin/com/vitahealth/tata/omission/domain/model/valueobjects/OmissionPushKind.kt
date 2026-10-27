package com.vitahealth.tata.omission.domain.model.valueobjects

/** The two notifications the backend Omission & Escalation context sends (TS-05 / TS-06). */
enum class OmissionPushKind {
    /** Reinforced reminder to the older adult while the intake is still pending (US-22). */
    REINFORCED_REMINDER,

    /** Alert to the caregivers once the intake is registered as omitted. */
    CAREGIVER_ALERT,
}
