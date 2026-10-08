package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import kotlinx.coroutines.CancellationException
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteDoseDetailRepository(
    private val api: IntakeApiService,
) : DoseDetailRepository {
    override suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel> =
        try {
            val response = api.getDoseDetail(intakeId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                mapIntakeDetail(body)
            } else {
                AppResult.Failure(
                    message = if (response.code() == 404) "No encontramos esta toma."
                    else "No pudimos consultar el detalle de la toma.",
                    code = if (response.code() == 404) "INTAKE_NOT_FOUND" else "REQUEST_FAILED",
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }

}
