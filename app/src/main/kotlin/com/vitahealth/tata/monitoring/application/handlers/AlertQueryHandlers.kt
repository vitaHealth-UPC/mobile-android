package com.vitahealth.tata.monitoring.application.handlers

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.AlertsRepository
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQuery
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQuery
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.common.result.AppResult

/** Pending alerts first, then attended and closed ones; the most recent intake first inside each group. */
internal val alertOrder: Comparator<CaregiverAlert> =
    compareBy<CaregiverAlert> { it.status.ordinal }.thenByDescending { it.scheduledAt }

class GetOpenAlertsQueryHandler(private val repository: AlertsRepository) {
    suspend operator fun invoke(query: GetOpenAlertsQuery): AppResult<List<CaregiverAlert>> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) return invalidReference()
        return when (val result = repository.openAlerts(query.caregiverId.trim(), query.olderAdultId.trim())) {
            is AppResult.Success -> AppResult.Success(result.value.sortedWith(alertOrder))
            is AppResult.Failure -> result
        }
    }
}

class GetAlertDetailQueryHandler(private val repository: AlertsRepository) {
    suspend operator fun invoke(query: GetAlertDetailQuery): AppResult<CaregiverAlert> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank() || query.alertId <= 0) return invalidReference()
        return repository.alert(query.caregiverId.trim(), query.olderAdultId.trim(), query.alertId)
    }
}

private fun invalidReference() = AppResult.Failure(
    message = "No se pudo identificar la alerta.",
    code = AlertFailureCodes.INVALID_REFERENCE,
)
