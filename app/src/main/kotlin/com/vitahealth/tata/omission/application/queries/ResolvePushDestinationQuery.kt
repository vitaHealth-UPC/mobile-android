package com.vitahealth.tata.omission.application.queries

import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind

/**
 * References supplied by a notification tap before recipient authorization and route resolution.
 */
data class ResolvePushDestinationQuery(
    val kind: OmissionPushKind,
    val olderAdultId: String,
    val medicationName: String? = null,
    val intakeId: String? = null,
)
