package com.vitahealth.tata.intake.application

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.common.result.AppResult

interface NextDoseRepository {
    suspend fun getNextDose(olderAdultId: String): AppResult<NextDoseReadModel?>
}
