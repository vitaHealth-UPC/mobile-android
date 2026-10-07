package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.AlertsRepository
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Response

/**
 * There is no alert list endpoint: the list comes from `openAlerts` of the recent status
 * (`GET /older-adults/{id}/status`) and the detail from `GET /older-adults/{id}/alerts/{alertId}`.
 */
class RemoteAlertsRepository(
    private val monitoringApi: FamilyMonitoringApiService,
    private val alertsApi: AlertsApiService,
) : AlertsRepository {

    override suspend fun openAlerts(caregiverId: String, olderAdultId: String): AppResult<List<CaregiverAlert>> = guarded {
        val response = monitoringApi.status(olderAdultId, caregiverId)
        val body = response.body()
        failureOf(response) ?: if (body == null) {
            failure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            AppResult.Success(body.openAlerts.orEmpty().map { it.toDomain() })
        }
    }

    override suspend fun alert(caregiverId: String, olderAdultId: String, alertId: Long): AppResult<CaregiverAlert> = guarded {
        val response = alertsApi.detail(olderAdultId, alertId, caregiverId)
        val body = response.body()
        failureOf(response) ?: if (body == null) {
            failure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            val alert = body.toDomain()
            if (alert.id != alertId) failure(AlertFailureCodes.INVALID_RESPONSE) else AppResult.Success(alert)
        }
    }

    override suspend fun updateStatus(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
        status: AlertStatus,
    ): AppResult<CaregiverAlert> = guarded {
        val response = alertsApi.updateStatus(olderAdultId, alertId, caregiverId, UpdateAlertStatusRequest(status.name))
        val body = response.body()
        failureOf(response) ?: if (body == null) {
            failure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            val alert = body.toDomain()
            if (alert.id != alertId) failure(AlertFailureCodes.INVALID_RESPONSE) else AppResult.Success(alert)
        }
    }

    private fun failureOf(response: Response<*>) = followUpHttpFailure(response, AlertFailureCodes.STATUS_NOT_ACCEPTED)

    private fun failure(code: String) = followUpFailure(code)

    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = guardedFollowUp(block)
}
