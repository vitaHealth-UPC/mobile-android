package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.ports.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult

/** Accepts only an acknowledged outcome for the intake submitted by the caller. */
class RemoteDoseConfirmationRepository(private val api: IntakeApiService) :
    DoseConfirmationRepository {
    override suspend fun confirm(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel> =
        try {
            val response =
                api.confirmDose(command.intakeId, ConfirmIntakeRequest(command.channel.name))
            val body = response.body()
            if (response.isSuccessful) {
                if (body == null || body.id != command.intakeId) invalidIntakeResponse()
                else
                    when (val mapped = mapIntakeDetail(body)) {
                        is AppResult.Failure -> mapped
                        is AppResult.Success ->
                            if (
                                mapped.value.status !in
                                    setOf(DoseStatus.CONFIRMED, DoseStatus.LATE) ||
                                    mapped.value.confirmedAt == null
                            )
                                invalidIntakeResponse()
                            else mapped
                    }
            } else
                when (response.code()) {
                    404 -> AppResult.Failure("No encontramos esta toma.", code = "INTAKE_NOT_FOUND")
                    409 ->
                        AppResult.Failure(
                            "Esta toma ya no puede confirmarse. Su historial se conserva.",
                            code = "INTAKE_NOT_CONFIRMABLE",
                        )
                    else -> intakeHttpFailure(response.code())
                }
        } catch (exception: Exception) {
            intakeRemoteFailure(exception)
        }
}
