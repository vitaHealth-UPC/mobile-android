package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.queries.GetDailyDoseProgressQuery
import com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult

class GetDailyDoseProgressQueryHandler(private val repository: IntakeAgendaRepository) {
    suspend operator fun invoke(query: GetDailyDoseProgressQuery): AppResult<DailyDoseProgress> {
        val adultId = query.olderAdultId.trim()
        if (adultId.isBlank()) return AppResult.Failure(
            message = "No se pudo identificar al adulto mayor.",
            code = "INVALID_OLDER_ADULT_REFERENCE",
        )
        val from = query.day.atStartOfDay(query.zone).toInstant()
        val to = query.day.plusDays(1).atStartOfDay(query.zone).toInstant()
        return when (val result = repository.getAgenda(adultId, from, to)) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val doses = result.value.filter {
                    it.olderAdultId == adultId && it.scheduledAt >= from && it.scheduledAt < to
                }.distinctBy { it.id }
                AppResult.Success(DailyDoseProgress(
                    completed = doses.count { it.status == DoseStatus.CONFIRMED || it.status == DoseStatus.LATE },
                    total = doses.size,
                ))
            }
        }
    }
}
