package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.domain.model.Medication

class RemoteTreatmentRepository(
    private val api: TreatmentApiService,
) : TreatmentRepository {
    override suspend fun registerMedication(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication> {
        return try {
            val response = api.registerMedication(
                olderAdultId = olderAdultId,
                request = RegisterMedicationRequest(
                    caregiverId = caregiverId,
                    name = name,
                    presentation = presentation,
                ),
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(
                    Medication(
                        id = body.id,
                        olderAdultId = body.olderAdultId,
                        name = body.name,
                        presentation = body.presentation,
                        active = body.active,
                    ),
                )
            } else {
                val (message, code) = when (response.code()) {
                    400 -> "Completa los datos obligatorios del medicamento." to "REQUEST_VALIDATION_FAILED"
                    403 -> "Necesitas un vínculo de cuidado activo para registrar medicamentos." to "CARE_LINK_NOT_AUTHORIZED"
                    404 -> "No encontramos el adulto mayor indicado." to "OLDER_ADULT_NOT_FOUND"
                    else -> "No pudimos registrar el medicamento." to "REQUEST_FAILED"
                }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No hay conexión disponible.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }
}
