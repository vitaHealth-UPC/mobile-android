package com.vitahealth.tata.treatment.application

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

interface TreatmentDetailRepository {
    suspend fun getTreatmentDetail(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<TreatmentDetailReadModel>
}
