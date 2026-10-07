package com.vitahealth.tata.monitoring.application

import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.common.result.AppResult

interface AlertsRepository {
    /** Alerts that still need attention, taken from the recent status of the older adult. */
    suspend fun openAlerts(caregiverId: String, olderAdultId: String): AppResult<List<CaregiverAlert>>
    suspend fun alert(caregiverId: String, olderAdultId: String, alertId: Long): AppResult<CaregiverAlert>
    suspend fun updateStatus(caregiverId: String, olderAdultId: String, alertId: Long, status: AlertStatus): AppResult<CaregiverAlert>
}

/** Stable failure codes of the alert reads; presentation maps them to its own messages. */
object AlertFailureCodes {
    const val ACCESS_DENIED = "CARE_RELATIONSHIP_REQUIRED"
    const val NOT_FOUND = "ALERT_NOT_FOUND"
    const val NETWORK = "NETWORK_UNAVAILABLE"
    const val INVALID_RESPONSE = "INVALID_RESPONSE"
    const val REQUEST_FAILED = "REQUEST_FAILED"
    const val INVALID_REFERENCE = "INVALID_ALERT_REFERENCE"
    const val STATUS_CONFLICT = "ALERT_STATUS_CONFLICT"
    const val STATUS_NOT_ACCEPTED = "ALERT_STATUS_NOT_ACCEPTED"
    const val NOTE_BLANK = "NOTE_BLANK"
    const val NOTE_TOO_LONG = "NOTE_TOO_LONG"
    const val NOTE_REJECTED = "INVALID_NOTE"
}
