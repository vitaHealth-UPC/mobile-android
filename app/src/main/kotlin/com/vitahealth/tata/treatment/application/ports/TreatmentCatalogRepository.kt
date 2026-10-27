package com.vitahealth.tata.treatment.application.ports

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

/** Provides the treatments available to a caregiver for the selected adult. */
interface TreatmentCatalogRepository {
    suspend fun listTreatments(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<TreatmentDetailReadModel>>
}
