package com.vitahealth.tata.monitoring

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.AlertsRepository
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.monitoring.infrastructure.remote.AlertSummaryResponse
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import java.time.Instant

internal fun alert(
    id: Long = 1,
    status: AlertStatus = AlertStatus.OPEN,
    scheduledAt: String = "2026-10-05T13:00:00Z",
) = CaregiverAlert(
    id = id,
    intakeId = "10$id",
    medicationName = "Losartan 50 mg",
    scheduledAt = Instant.parse(scheduledAt),
    reason = "Intake not confirmed within the grace period",
    status = status,
    openedAt = Instant.parse(scheduledAt).plusSeconds(1800),
    closedAt = if (status == AlertStatus.CLOSED) Instant.parse(scheduledAt).plusSeconds(3600) else null,
)

/** Values of the `AlertSummaryResource` example published in the backend Swagger. */
internal fun swaggerAlertResponse(
    id: Long? = 1,
    status: String? = "OPEN",
    closedAt: String? = null,
) = AlertSummaryResponse(
    id = id,
    intakeId = "101",
    medicationName = "Losartan 50 mg",
    scheduledAt = "2026-10-05T13:00:00Z",
    reason = "Intake not confirmed within the grace period",
    status = status,
    openedAt = "2026-10-05T13:30:00Z",
    closedAt = closedAt,
)

internal fun failure(code: String) = AppResult.Failure(message = code, code = code)

internal class FakeAlertsRepository : AlertsRepository {
    var list: AppResult<List<CaregiverAlert>> = AppResult.Success(emptyList())
    var detail: AppResult<CaregiverAlert> = failure(AlertFailureCodes.NOT_FOUND)
    var update: AppResult<CaregiverAlert> = failure(AlertFailureCodes.REQUEST_FAILED)
    /** When set, reads suspend until the test completes it, so the loading state can be observed. */
    var gate: CompletableDeferred<Unit>? = null
    val requests = mutableListOf<String>()

    override suspend fun openAlerts(caregiverId: String, olderAdultId: String): AppResult<List<CaregiverAlert>> {
        requests += "list:$caregiverId:$olderAdultId"
        gate?.await()
        return list
    }

    override suspend fun alert(caregiverId: String, olderAdultId: String, alertId: Long): AppResult<CaregiverAlert> {
        requests += "detail:$caregiverId:$olderAdultId:$alertId"
        gate?.await()
        return detail
    }

    override suspend fun updateStatus(caregiverId: String, olderAdultId: String, alertId: Long, status: AlertStatus): AppResult<CaregiverAlert> {
        requests += "update:$caregiverId:$olderAdultId:$alertId:$status"
        gate?.await()
        return update
    }
}
