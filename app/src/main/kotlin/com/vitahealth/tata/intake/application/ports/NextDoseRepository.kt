package com.vitahealth.tata.intake.application.ports

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads the next available scheduled dose, or absence when none remains. */
interface NextDoseRepository {
    suspend fun getNextDose(olderAdultId: String): AppResult<NextDoseReadModel?>
}
