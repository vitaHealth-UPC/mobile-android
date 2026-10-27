package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.ports.DoseDetailRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Fetches a validated detail for the exact intake requested by the caller. */
class RemoteDoseDetailRepository(private val api: IntakeApiService) : DoseDetailRepository {
    override suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel> =
        try {
            val response = api.getDoseDetail(intakeId)
            val body = response.body()
            if (response.isSuccessful) {
                if (body == null || body.id != intakeId) invalidIntakeResponse()
                else mapIntakeDetail(body)
            } else {
                intakeHttpFailure(response.code())
            }
        } catch (exception: Exception) {
            intakeRemoteFailure(exception)
        }
}
