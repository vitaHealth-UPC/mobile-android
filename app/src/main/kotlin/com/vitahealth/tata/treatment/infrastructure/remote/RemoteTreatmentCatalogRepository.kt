package com.vitahealth.tata.treatment.infrastructure.remote
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentCatalogRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus
import kotlinx.coroutines.CancellationException
class RemoteTreatmentCatalogRepository(private val api: TreatmentApiService): TreatmentCatalogRepository {
    override suspend fun listTreatments(caregiverId: String,olderAdultId: String): AppResult<List<TreatmentDetailReadModel>> = try {
        val response=api.listTreatments(olderAdultId,caregiverId)
        val body=response.body()
        if(response.isSuccessful && body!=null) AppResult.Success(body.map{dto->TreatmentDetailReadModel(dto.id,dto.olderAdultId,dto.name,TreatmentStatus.valueOf(dto.status),dto.medicationId,dto.dose,dto.frequency,dto.scheduledTimes.orEmpty(),dto.instructions.orEmpty(),dto.reminderLeadMinutes)})
        else AppResult.Failure("Treatment request failed",code=if(response.code()==403) "CARE_LINK_NOT_AUTHORIZED" else "REQUEST_FAILED")
    } catch(exception: CancellationException){throw exception} catch(exception: java.io.IOException){AppResult.Failure("Network unavailable",code="NETWORK_UNAVAILABLE")} catch(exception: RuntimeException){AppResult.Failure("Invalid response",code="INVALID_RESPONSE")}
}
