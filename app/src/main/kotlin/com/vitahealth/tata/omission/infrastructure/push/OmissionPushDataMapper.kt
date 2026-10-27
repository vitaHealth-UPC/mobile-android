package com.vitahealth.tata.omission.infrastructure.push

import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
import java.time.Instant

/** Maps the backend's versioned FCM data contract without parsing localized notification text. */
internal fun Map<String, String>.toOmissionPush(): OmissionPush? {
    if (get("schemaVersion") != "1") return null
    val kind = OmissionPushKind.entries.firstOrNull { it.name == get("kind") } ?: return null
    val adult = get("olderAdultId")?.takeIf { it.isNotBlank() } ?: return null
    val delivery = get("deliveryId")?.takeIf { it.isNotBlank() } ?: return null
    val intake = get("intakeId")?.takeIf { it.isNotBlank() } ?: return null
    val medication = get("medicationName")?.takeIf { it.isNotBlank() } ?: return null
    val scheduled = runCatching { Instant.parse(get("scheduledAt")) }.getOrNull() ?: return null
    return OmissionPush(kind, adult, medication, delivery, intake, scheduled)
}
