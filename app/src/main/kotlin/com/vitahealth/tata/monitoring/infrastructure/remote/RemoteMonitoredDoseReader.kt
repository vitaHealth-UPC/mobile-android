package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.ports.MonitoredDoseReader
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteMonitoredDoseReader(private val api: FamilyMonitoringApiService) : MonitoredDoseReader {
    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        intakeId: String,
    ): AppResult<MonitoredDose> = guardedFollowUp {
        val response = api.dose(intakeId)
        followUpHttpFailure(response)?.let {
            return@guardedFollowUp it
        }
        val dose = requireNotNull(response.body())
        require(dose.id == intakeId && dose.olderAdultId == olderAdultId)
        AppResult.Success(dose.toMonitoredDose())
    }
}
