package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.ports.NextDoseRepository
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads the requested adult's pending dose; HTTP 204 alone represents an empty schedule. */
class RemoteNextDoseRepository(private val api: IntakeApiService) : NextDoseRepository {
    override suspend fun getNextDose(olderAdultId: String): AppResult<NextDoseReadModel?> =
        try {
            val response = api.getNextDose(olderAdultId)
            if (response.code() == 204) {
                AppResult.Success(null)
            } else if (!response.isSuccessful) {
                intakeHttpFailure(response.code(), "OLDER_ADULT_NOT_FOUND")
            } else {
                val body = response.body()
                if (body == null || body.olderAdultId != olderAdultId) invalidIntakeResponse()
                else
                    when (val mapped = mapIntakeDetail(body)) {
                        is AppResult.Failure -> mapped
                        is AppResult.Success -> {
                            val dose = mapped.value
                            if (dose.status != DoseStatus.PENDING) invalidIntakeResponse()
                            else
                                AppResult.Success(
                                    NextDoseReadModel(
                                        id = dose.id,
                                        treatmentId = dose.treatmentId,
                                        medicationId = dose.medicationId,
                                        olderAdultId = dose.olderAdultId,
                                        medicationName = dose.medicationName,
                                        dose = dose.dose,
                                        instructions = dose.instructions,
                                        scheduledAt = dose.scheduledAt,
                                        status = dose.status,
                                    )
                                )
                        }
                    }
            }
        } catch (exception: Exception) {
            intakeRemoteFailure(exception)
        }
}
