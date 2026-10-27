package com.vitahealth.tata.monitoring.application.ports

import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.shared.common.result.AppResult

fun interface AlertHistoryReader {
    suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
    ): AppResult<List<RecordedAlertEvent>>
}
