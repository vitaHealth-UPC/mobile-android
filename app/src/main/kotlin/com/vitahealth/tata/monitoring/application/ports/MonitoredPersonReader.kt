package com.vitahealth.tata.monitoring.application.ports

import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.shared.common.result.AppResult

fun interface MonitoredPersonReader {
    suspend fun read(caregiverId: String, olderAdultId: String): AppResult<MonitoredPerson>
}
