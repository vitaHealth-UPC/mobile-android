package com.vitahealth.tata.treatment.application
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
interface TreatmentCatalogRepository { suspend fun listTreatments(caregiverId: String,olderAdultId: String): AppResult<List<TreatmentDetailReadModel>> }
