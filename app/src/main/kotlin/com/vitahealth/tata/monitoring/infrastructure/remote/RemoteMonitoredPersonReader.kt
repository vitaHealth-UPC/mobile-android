package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.ports.MonitoredPersonReader
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate

class RemoteMonitoredPersonReader(private val api: FamilyMonitoringApiService) :
    MonitoredPersonReader {
    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<MonitoredPerson> = guardedFollowUp {
        val response = api.profile(olderAdultId)
        followUpHttpFailure(response)?.let {
            return@guardedFollowUp it
        }
        val profile = requireNotNull(response.body())
        require(profile.id == olderAdultId && !profile.fullName.isNullOrBlank())
        val birthDate =
            try {
                profile.birthDate?.let(LocalDate::parse)
            } catch (error: java.time.DateTimeException) {
                throw IllegalArgumentException("Invalid profile birth date", error)
            }
        require(birthDate == null || !birthDate.isAfter(LocalDate.now()))
        AppResult.Success(
            MonitoredPerson(olderAdultId, requireNotNull(profile.fullName), birthDate)
        )
    }
}
