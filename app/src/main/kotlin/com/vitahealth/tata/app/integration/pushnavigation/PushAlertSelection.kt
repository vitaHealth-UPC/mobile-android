package com.vitahealth.tata.app.integration.pushnavigation

import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus

/**
 * Selects the referenced intake's open alert. Legacy pushes can match a medication; an explicit
 * reference never falls back to an unrelated alert.
 */
internal fun alertForPush(
    alerts: List<CaregiverAlert>,
    medicationName: String?,
    intakeId: String? = null,
): CaregiverAlert? {
    val open = alerts.filter { it.status == AlertStatus.OPEN }.sortedByDescending { it.openedAt }
    val medication = medicationName?.trim()?.takeIf { it.isNotBlank() }
    if (intakeId != null) return open.firstOrNull { it.intakeId == intakeId }
    return if (medication != null)
        open.firstOrNull { it.medicationName.equals(medication, ignoreCase = true) }
    else open.firstOrNull()
}
