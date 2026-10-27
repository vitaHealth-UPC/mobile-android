package com.vitahealth.tata.monitoring.presentation.alerts.viewmodel

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import java.time.Instant
import java.time.LocalDate

/** Why an alert read failed, as the screens explain it. */
enum class AlertsProblem {
    NETWORK,
    ACCESS_DENIED,
    NOT_FOUND,
    CONFLICT,
    UNKNOWN,
}

internal fun problemOf(code: String?): AlertsProblem =
    when (code) {
        AlertFailureCodes.NETWORK -> AlertsProblem.NETWORK
        "CARE_RELATIONSHIP_REQUIRED",
        "SESSION_CHANGED",
        AlertFailureCodes.SESSION_REQUIRED,
        AlertFailureCodes.ACCESS_DENIED -> AlertsProblem.ACCESS_DENIED
        AlertFailureCodes.NOT_FOUND -> AlertsProblem.NOT_FOUND
        AlertFailureCodes.STATUS_CONFLICT -> AlertsProblem.CONFLICT
        else -> AlertsProblem.UNKNOWN
    }

/** Filters follow-up alerts and scheduled dose events without conflating their statuses. */
enum class AlertFilter(val statuses: Set<AlertStatus>) {
    ALL(AlertStatus.entries.toSet()),
    PENDING(setOf(AlertStatus.OPEN)),
    UPCOMING(emptySet()),
    COMPLETED(setOf(AlertStatus.ATTENDED, AlertStatus.CLOSED)),
}

sealed interface AlertsUiState {
    data object Loading : AlertsUiState

    data object Empty : AlertsUiState

    data class Content(
        val alerts: List<CaregiverAlert>,
        val filter: AlertFilter = AlertFilter.ALL,
        val doses: List<MonitoredDose> = emptyList(),
        val olderAdultName: String = "",
        val day: LocalDate = LocalDate.now(),
        val asOf: Instant = Instant.now(),
    ) : AlertsUiState {
        val visibleAlerts: List<CaregiverAlert>
            get() = alerts.filter { it.status in filter.statuses }

        val visibleDoses: List<MonitoredDose>
            get() =
                doses.filter {
                    when (filter) {
                        AlertFilter.PENDING -> false
                        AlertFilter.UPCOMING ->
                            it.status == "PENDING" && it.scheduledAt.isAfter(asOf)
                        AlertFilter.COMPLETED -> it.status == "CONFIRMED" || it.status == "LATE"
                        AlertFilter.ALL ->
                            (it.status == "PENDING" && it.scheduledAt.isAfter(asOf)) ||
                                it.status == "CONFIRMED" ||
                                it.status == "LATE"
                    }
                }

        val pendingCount: Int
            get() = alerts.count { it.status == AlertStatus.OPEN }
    }

    data class Error(val problem: AlertsProblem) : AlertsUiState
}

sealed interface AlertDetailUiState {
    data object Loading : AlertDetailUiState

    /** [updating] is the status being sent; [feedback] reports the last follow-up action. */
    data class Content(
        val alert: CaregiverAlert,
        val updating: AlertStatus? = null,
        val dose: MonitoredDose? = null,
        val feedback: AlertFeedback? = null,
    ) : AlertDetailUiState

    data class Error(val problem: AlertsProblem) : AlertDetailUiState
}

/** Contact offer of the alert detail (US-29); a missing channel is [Ready] with a null channel. */
sealed interface ContactUiState {
    data object Loading : ContactUiState

    data class Ready(val option: ContactOption) : ContactUiState

    data class Failed(val problem: AlertsProblem) : ContactUiState
}

/** Outcome of a follow-up action on the alert detail. */
sealed interface AlertFeedback {
    data class StatusChanged(val status: AlertStatus) : AlertFeedback

    data class Failed(val problem: AlertsProblem) : AlertFeedback
}
