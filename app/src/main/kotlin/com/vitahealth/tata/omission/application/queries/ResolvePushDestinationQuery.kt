package com.vitahealth.tata.omission.application.queries

import com.vitahealth.tata.omission.domain.model.OmissionPushKind

data class ResolvePushDestinationQuery(
    val kind: OmissionPushKind,
    val olderAdultId: String,
    val medicationName: String? = null,
)

/** Screen a tapped omission notification opens. */
sealed interface PushDestination {
    /**
     * Caregiver alerts of the older adult (US-27). The push carries no alert id: the app opens the list and,
     * when it finds the open alert of [medicationName], its detail.
     */
    data class CaregiverAlerts(val caregiverId: String, val olderAdultId: String, val medicationName: String? = null) : PushDestination

    /** Home of the older adult, where the pending intake can be confirmed. */
    data class OlderAdultHome(val olderAdultId: String, val olderAdultName: String) : PushDestination
}
