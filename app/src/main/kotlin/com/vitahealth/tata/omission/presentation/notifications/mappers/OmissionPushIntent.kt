package com.vitahealth.tata.omission.presentation.notifications.mappers

import com.vitahealth.tata.omission.application.queries.ResolvePushDestinationQuery
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind

/** Extras a tapped omission notification puts on the launch intent of the app. */
object OmissionPushIntent {
    const val EXTRA_KIND = "com.vitahealth.tata.omission.KIND"
    const val EXTRA_OLDER_ADULT_ID = "com.vitahealth.tata.omission.OLDER_ADULT_ID"
    const val EXTRA_MEDICATION = "com.vitahealth.tata.omission.MEDICATION"
    const val EXTRA_INTAKE_ID = "com.vitahealth.tata.omission.INTAKE_ID"

    /** Null for any intent that was not opened from an omission notification. */
    fun queryFrom(
        kind: String?,
        olderAdultId: String?,
        medicationName: String? = null,
        intakeId: String? = null,
    ): ResolvePushDestinationQuery? {
        val pushKind = OmissionPushKind.entries.firstOrNull { it.name == kind } ?: return null
        val id = olderAdultId?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (intakeId != null && intakeId.isBlank()) return null
        return ResolvePushDestinationQuery(
            pushKind,
            id,
            medicationName?.trim()?.takeIf { it.isNotBlank() },
            intakeId?.trim(),
        )
    }
}
