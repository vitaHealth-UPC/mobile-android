package com.vitahealth.tata.app.navigation

import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert

/**
 * Alert a caregiver push points to. The push has no alert id, only the medication in its text, so this
 * takes the most recent OPEN alert of that medication, or the most recent OPEN alert when none matches.
 */
internal fun alertForPush(alerts: List<CaregiverAlert>, medicationName: String?): CaregiverAlert? {
    val open = alerts.filter { it.status == AlertStatus.OPEN }.sortedByDescending { it.openedAt }
    val medication = medicationName?.trim()?.takeIf { it.isNotBlank() }
    return medication?.let { name -> open.firstOrNull { it.medicationName.equals(name, ignoreCase = true) } }
        ?: open.firstOrNull()
}
