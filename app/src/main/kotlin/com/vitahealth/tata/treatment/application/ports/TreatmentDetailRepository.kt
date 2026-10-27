package com.vitahealth.tata.treatment.application.ports

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

/** Reads one treatment and its configured regimen for an authorized caregiver. */
interface TreatmentDetailRepository {
    suspend fun getTreatmentDetail(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<TreatmentDetailReadModel>
}
