package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.MedicationManagementRepository
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.application.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.application.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.treatment.domain.model.Treatment
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus

class RemoteTreatmentRepository(
    private val api: TreatmentApiService,
) : TreatmentRepository, TreatmentLifecycleRepository, TreatmentDetailRepository, MedicationManagementRepository {
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

    override suspend fun listMedications(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<Medication>> =
        try {
            val response = api.listMedications(olderAdultId = olderAdultId, caregiverId = caregiverId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.map { it.toDomain() })
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            networkFailure(exception)
        }

    override suspend fun updateMedication(
        caregiverId: String,
        medicationId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication> =
        medicationRequest {
            api.updateMedication(
                medicationId = medicationId,
                request = UpdateMedicationRequest(
                    caregiverId = caregiverId,
                    name = name,
                    presentation = presentation,
                ),
            )
        }

    override suspend fun deactivateMedication(
        caregiverId: String,
        medicationId: String,
    ): AppResult<Medication> =
        medicationRequest { api.deactivateMedication(medicationId, caregiverId) }

    private suspend fun medicationRequest(
        request: suspend () -> retrofit2.Response<MedicationResponse>,
    ): AppResult<Medication> =
        try {
            val response = request()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            networkFailure(exception)
        }

    private fun MedicationResponse.toDomain() = Medication(
        id = id,
        olderAdultId = olderAdultId,
        name = name,
        presentation = presentation,
        active = active,
    )

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
                val status = runCatching { TreatmentStatus.valueOf(body.status) }.getOrNull()
                if (status == null) {
                    AppResult.Failure(
                        message = "El servicio devolvió un estado de tratamiento no reconocido.",
                        code = "INVALID_TREATMENT_STATE",
                    )
                } else {
                    AppResult.Success(
                        Treatment(
                            id = body.id,
                            olderAdultId = body.olderAdultId,
                            name = body.name,
                            status = status,
                        ),
                    )
                }
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


    override suspend fun configureTreatment(
        caregiverId: String,
        treatmentId: String,
        medicationId: String,
        dose: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String,
        reminderLeadMinutes: Int,
    ): AppResult<Treatment> =
        treatmentRequest {
            api.configureTreatment(
                treatmentId = treatmentId,
                request = ConfigureTreatmentRequest(
                    caregiverId = caregiverId,
                    medicationId = medicationId,
                    dose = dose,
                    frequency = frequency,
                    scheduledTimes = scheduledTimes,
                    instructions = instructions,
                    reminderLeadMinutes = reminderLeadMinutes,
                ),
            )
        }

    override suspend fun activateTreatment(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<Treatment> =
        treatmentRequest { api.activateTreatment(treatmentId, caregiverId) }

    override suspend fun pauseTreatment(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<Treatment> =
        treatmentRequest { api.pauseTreatment(treatmentId, caregiverId) }

    override suspend fun resumeTreatment(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<Treatment> =
        treatmentRequest { api.resumeTreatment(treatmentId, caregiverId) }

    private suspend fun treatmentRequest(
        request: suspend () -> retrofit2.Response<TreatmentResponse>,
    ): AppResult<Treatment> {
        return try {
            val response = request()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val status = runCatching { TreatmentStatus.valueOf(body.status) }.getOrNull()
                    ?: return AppResult.Failure(
                        message = "El servicio devolvió un estado de tratamiento no reconocido.",
                        code = "INVALID_TREATMENT_STATE",
                    )
                AppResult.Success(
                    Treatment(
                        id = body.id,
                        olderAdultId = body.olderAdultId,
                        name = body.name,
                        status = status,
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


    override suspend fun getTreatmentDetail(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<TreatmentDetailReadModel> {
        return try {
            val response = api.getTreatmentDetail(
                treatmentId = treatmentId,
                caregiverId = caregiverId,
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val status = runCatching { TreatmentStatus.valueOf(body.status) }.getOrNull()
                    ?: return AppResult.Failure(
                        message = "El servicio devolvió un estado de tratamiento no reconocido.",
                        code = "INVALID_TREATMENT_STATE",
                    )
                AppResult.Success(
                    TreatmentDetailReadModel(
                        id = body.id,
                        olderAdultId = body.olderAdultId,
                        name = body.name,
                        status = status,
                        medicationId = body.medicationId,
                        dose = body.dose,
                        frequency = body.frequency,
                        scheduledTimes = body.scheduledTimes.orEmpty(),
                        instructions = body.instructions.orEmpty(),
                        reminderLeadMinutes = body.reminderLeadMinutes,
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

    private fun networkFailure(exception: Exception): AppResult.Failure {
        if(exception is kotlinx.coroutines.CancellationException) throw exception
        return AppResult.Failure(
            message = "No hay conexión disponible.",
            cause = exception,
            code = "NETWORK_UNAVAILABLE",
        )
    }
}
