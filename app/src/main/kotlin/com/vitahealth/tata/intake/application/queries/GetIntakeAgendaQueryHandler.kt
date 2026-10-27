package com.vitahealth.tata.intake.application.queries

import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Validates owner and time window before reading the agenda projection. */
class GetIntakeAgendaQueryHandler(private val repository: IntakeAgendaRepository) {

    suspend operator fun invoke(
        query: GetIntakeAgendaQuery
    ): AppResult<List<DoseDetailReadModel>> {
        val adultId = query.olderAdultId.trim()
        if (adultId.isBlank()) {
            return AppResult.Failure(
                "No se pudo identificar al adulto mayor.",
                code = "INVALID_OLDER_ADULT_REFERENCE",
            )
        }
        if (!query.from.isBefore(query.to)) {
            return AppResult.Failure(
                "El periodo de la agenda no es valido.",
                code = "INVALID_AGENDA_WINDOW",
            )
        }
        return repository.getAgenda(adultId, query.from, query.to)
    }
}
