package com.vitahealth.tata.treatment.domain.repositories

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Treatment

/** Persists regimen configuration and treatment lifecycle transitions. */
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
        followUpDelayMinutes: Int? = null,
    ): AppResult<Treatment>

    suspend fun activateTreatment(caregiverId: String, treatmentId: String): AppResult<Treatment>

    suspend fun pauseTreatment(caregiverId: String, treatmentId: String): AppResult<Treatment>

    suspend fun resumeTreatment(caregiverId: String, treatmentId: String): AppResult<Treatment>
}
