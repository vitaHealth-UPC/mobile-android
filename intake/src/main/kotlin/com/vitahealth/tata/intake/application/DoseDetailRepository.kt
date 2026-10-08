package com.vitahealth.tata.intake.application

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

interface DoseDetailRepository {
    suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel>
}
