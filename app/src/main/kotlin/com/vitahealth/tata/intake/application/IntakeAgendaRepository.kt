package com.vitahealth.tata.intake.application

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

interface IntakeAgendaRepository {
    suspend fun getAgenda(olderAdultId: String, from: Instant, to: Instant): AppResult<List<DoseDetailReadModel>>
}
