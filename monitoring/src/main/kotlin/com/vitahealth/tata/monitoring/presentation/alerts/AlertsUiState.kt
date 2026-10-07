package com.vitahealth.tata.monitoring.presentation.alerts

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert

/** Why an alert read failed, as the screens explain it. */
enum class AlertsProblem { NETWORK, ACCESS_DENIED, NOT_FOUND, UNKNOWN }

internal fun problemOf(code: String?): AlertsProblem = when (code) {
    AlertFailureCodes.NETWORK -> AlertsProblem.NETWORK
    AlertFailureCodes.ACCESS_DENIED -> AlertsProblem.ACCESS_DENIED
    AlertFailureCodes.NOT_FOUND -> AlertsProblem.NOT_FOUND
    else -> AlertsProblem.UNKNOWN
}

/** Filters the alert list can back with real data: `openAlerts` only carries OPEN and ATTENDED alerts. */
enum class AlertFilter(val statuses: Set<AlertStatus>) {
    ALL(AlertStatus.entries.toSet()),
    PENDING(setOf(AlertStatus.OPEN)),
    ATTENDED(setOf(AlertStatus.ATTENDED)),
}

sealed interface AlertsUiState {
    data object Loading : AlertsUiState
    data object Empty : AlertsUiState
    data class Content(
        val alerts: List<CaregiverAlert>,
        val filter: AlertFilter = AlertFilter.ALL,
    ) : AlertsUiState {
        val visibleAlerts: List<CaregiverAlert> get() = alerts.filter { it.status in filter.statuses }
        val pendingCount: Int get() = alerts.count { it.status == AlertStatus.OPEN }
    }
    data class Error(val problem: AlertsProblem) : AlertsUiState
}

sealed interface AlertDetailUiState {
    data object Loading : AlertDetailUiState
    data class Content(val alert: CaregiverAlert) : AlertDetailUiState
    data class Error(val problem: AlertsProblem) : AlertDetailUiState
}
