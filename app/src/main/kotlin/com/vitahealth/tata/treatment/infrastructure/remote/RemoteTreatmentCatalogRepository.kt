package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentCatalogRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/** Loads treatment details belonging to the selected adult through the catalog endpoint. */
class RemoteTreatmentCatalogRepository(private val api: TreatmentApiService) :
    TreatmentCatalogRepository {
    override suspend fun listTreatments(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<TreatmentDetailReadModel>> =
        try {
            val response = api.listTreatments(olderAdultId, caregiverId)
            if (response.isSuccessful) {
                val body = requireNotNull(response.body())
                require(body.all { it.olderAdultId == olderAdultId })
                require(body.map { it.id }.distinct().size == body.size)
                val treatments =
                    body.map { resource ->
                        val status =
                            TreatmentStatus.entries.find { it.name == resource.status }
                                ?: return AppResult.Failure(
                                    "Unknown treatment state",
                                    code = "INVALID_TREATMENT_STATE",
                                )
                        resource.toDetailReadModel(status)
                    }
                AppResult.Success(treatments)
            } else {
                AppResult.Failure(
                    treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            requestFailure(exception)
        }
}
