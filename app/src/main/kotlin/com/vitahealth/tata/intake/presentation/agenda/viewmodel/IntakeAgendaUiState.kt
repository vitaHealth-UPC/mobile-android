package com.vitahealth.tata.intake.presentation.agenda.viewmodel

import com.vitahealth.tata.intake.application.queries.AgendaWeek
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import java.time.LocalDate
import java.time.ZoneId

/** Selected agenda week, day and corresponding dose evidence. */
data class IntakeAgendaUiState(
    val week: AgendaWeek = AgendaWeek.containing(LocalDate.now(), ZoneId.systemDefault()),
    val selectedDay: LocalDate = LocalDate.now(),
    val doses: List<DoseDetailReadModel> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
) {
    val selectedDoses
        get() = doses.filter { it.scheduledAt.atZone(week.zone).toLocalDate() == selectedDay }
}
