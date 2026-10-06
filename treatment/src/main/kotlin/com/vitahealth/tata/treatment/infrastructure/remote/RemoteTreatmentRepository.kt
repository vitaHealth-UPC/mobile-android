package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.treatment.domain.model.Treatment
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus

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
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            networkFailure(exception)
        }
    }

    override suspend fun createTreatment(
        caregiverId: String,
        olderAdultId: String,
        name: String,
    ): AppResult<Treatment> {
        return try {
            val response = api.createTreatment(
                olderAdultId = olderAdultId,
                request = CreateTreatmentRequest(
                    caregiverId = caregiverId,
                    name = name,
                ),
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(
                    Treatment(
                        id = body.id,
                        olderAdultId = body.olderAdultId,
                        name = body.name,
                        status = runCatching { TreatmentStatus.valueOf(body.status) }
                            .getOrDefault(TreatmentStatus.INCOMPLETE),
                    ),
                )
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            networkFailure(exception)
        }
    }

    private fun treatmentMessage(status: Int): String =
        when (status) {
            400 -> "Revisa los datos obligatorios."
            403 -> "Necesitas un vínculo de cuidado activo para continuar."
            404 -> "No encontramos el recurso solicitado."
            409 -> "El tratamiento no puede cambiar a ese estado."
            else -> "No pudimos completar la solicitud."
        }

    private fun treatmentCode(status: Int): String =
        when (status) {
            400 -> "REQUEST_VALIDATION_FAILED"
            403 -> "CARE_LINK_NOT_AUTHORIZED"
            404 -> "RESOURCE_NOT_FOUND"
            409 -> "CONFLICT"
            else -> "REQUEST_FAILED"
        }

    private fun networkFailure(exception: Exception): AppResult.Failure =
        AppResult.Failure(
            message = "No hay conexión disponible.",
            cause = exception,
            code = "NETWORK_UNAVAILABLE",
        )
}
