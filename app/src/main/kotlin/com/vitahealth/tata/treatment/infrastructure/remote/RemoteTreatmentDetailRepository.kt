package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/** Retrieves validated detail for a single treatment. */
class RemoteTreatmentDetailRepository(private val api: TreatmentApiService) :
    TreatmentDetailRepository {
    override suspend fun getTreatmentDetail(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<TreatmentDetailReadModel> {
        return try {
            val response =
                api.getTreatmentDetail(treatmentId = treatmentId, caregiverId = caregiverId)
            if (response.isSuccessful) {
                val body = requireNotNull(response.body()) { "Missing treatment resource" }
                require(body.id == treatmentId)
                val status =
                    runCatching { TreatmentStatus.valueOf(body.status) }.getOrNull()
                        ?: return AppResult.Failure(
                            message =
                                "El servicio devolvió un estado de tratamiento no reconocido.",
                            code = "INVALID_TREATMENT_STATE",
                        )
                AppResult.Success(body.toDetailReadModel(status))
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
