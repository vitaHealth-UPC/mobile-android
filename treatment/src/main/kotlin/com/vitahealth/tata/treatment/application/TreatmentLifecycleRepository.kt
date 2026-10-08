package com.vitahealth.tata.treatment.application

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.Treatment

interface TreatmentLifecycleRepository {
    suspend fun configureTreatment(
        caregiverId: String,
        treatmentId: String,
        medicationId: String,
        dose: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String,
        reminderLeadMinutes: Int,
    ): AppResult<Treatment>

    suspend fun activateTreatment(caregiverId: String, treatmentId: String): AppResult<Treatment>
    suspend fun pauseTreatment(caregiverId: String, treatmentId: String): AppResult<Treatment>
    suspend fun resumeTreatment(caregiverId: String, treatmentId: String): AppResult<Treatment>
}
