package com.vitahealth.tata.intake.application.ports

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

/** Reads scheduled intakes for an adult within an absolute time interval. */
interface IntakeAgendaRepository {
    suspend fun getAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
    ): AppResult<List<DoseDetailReadModel>>
}
