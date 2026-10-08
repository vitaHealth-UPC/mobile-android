package com.vitahealth.tata.identity.domain.model

/** What a plan enables. The backend decides which plan has which capability. */
enum class PlanCapability {
    REMINDERS,
    AGENDA,
    INTAKE_CONFIRMATION,
    FAMILY_ALERTS,
    FAMILY_MONITORING,
    ADHERENCE_INSIGHTS,
}
