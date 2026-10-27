package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.TreatmentRepository

/** Registers medications and creates treatment drafts. */
class RemoteTreatmentRepository(private val api: TreatmentApiService) : TreatmentRepository {
    override suspend fun registerMedication(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication> {
        return try {
            val response =
                api.registerMedication(
                    olderAdultId = olderAdultId,
                    request =
                        RegisterMedicationRequest(
                            caregiverId = caregiverId,
                            name = name,
                            presentation = presentation,
                        ),
                )
            if (response.isSuccessful) {
                val body = requireNotNull(response.body()) { "Missing treatment resource" }
                require(body.olderAdultId == olderAdultId)
                require(body.name == name.trim() && body.presentation == presentation.trim())
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            requestFailure(exception)
        }
    }

    override suspend fun createTreatment(
        caregiverId: String,
        olderAdultId: String,
        name: String,
    ): AppResult<Treatment> {
        return try {
            val response =
                api.createTreatment(
                    olderAdultId = olderAdultId,
                    request = CreateTreatmentRequest(caregiverId = caregiverId, name = name),
                )
            if (response.isSuccessful) {
                val body = requireNotNull(response.body()) { "Missing treatment resource" }
                require(body.olderAdultId == olderAdultId)
                require(body.name == name.trim())
                val status = runCatching { TreatmentStatus.valueOf(body.status) }.getOrNull()
                if (status == null) {
                    AppResult.Failure(
                        message = "El servicio devolvió un estado de tratamiento no reconocido.",
                        code = "INVALID_TREATMENT_STATE",
                    )
                } else {
                    AppResult.Success(body.toDomain(status))
                }
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            requestFailure(exception)
        }
    }
}
