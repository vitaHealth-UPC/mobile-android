package com.vitahealth.tata.monitoring.application.ports

import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.shared.common.result.AppResult

fun interface MonitoredDoseReader {
    suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        intakeId: String,
    ): AppResult<MonitoredDose>
}
