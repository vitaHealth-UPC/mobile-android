package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException

class RemoteDoseConfirmationRepository(private val api: IntakeApiService) : DoseConfirmationRepository {
    override suspend fun confirm(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel> = try {
        val response = api.confirmDose(command.intakeId, ConfirmIntakeRequest(command.channel.name))
        val body = response.body()
        if (response.isSuccessful && body != null) mapIntakeDetail(body)
        else when (response.code()) {
            404 -> AppResult.Failure("No encontramos esta toma.", code = "INTAKE_NOT_FOUND")
            409 -> AppResult.Failure("Esta toma ya no puede confirmarse. Su historial se conserva.", code = "INTAKE_NOT_CONFIRMABLE")
            else -> AppResult.Failure("No pudimos confirmar la toma. Inténtalo nuevamente.", code = "REQUEST_FAILED")
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure("No hay conexión. Puedes reintentar sin duplicar la toma.", cause = exception, code = "NETWORK_UNAVAILABLE")
    }
}
