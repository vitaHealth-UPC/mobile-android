package com.vitahealth.tata.monitoring.domain.repositories

import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads caregiver alerts and applies allowed status transitions. */
interface AlertsRepository {
    /** Alerts that still need attention, taken from the recent status of the older adult. */
    suspend fun openAlerts(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<CaregiverAlert>>

    suspend fun alert(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
    ): AppResult<CaregiverAlert>

    suspend fun updateStatus(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
        status: AlertStatus,
    ): AppResult<CaregiverAlert>
}
