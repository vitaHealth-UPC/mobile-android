package com.vitahealth.tata.intake.presentation.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.queries.AgendaWeek
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class IntakeAgendaUiState(
    val week: AgendaWeek = AgendaWeek.containing(LocalDate.now(), ZoneId.systemDefault()),
    val selectedDay: LocalDate = LocalDate.now(),
    val doses: List<DoseDetailReadModel> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
) {
    val selectedDoses get() = doses.filter { it.scheduledAt.atZone(week.zone).toLocalDate() == selectedDay }
}

class IntakeAgendaViewModel(private val olderAdultId: String, private val repository: IntakeAgendaRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(IntakeAgendaUiState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null
    fun selectDay(day: LocalDate) { if (day in mutableState.value.week.days) mutableState.value = mutableState.value.copy(selectedDay = day) }
    fun moveWeek(offset: Long) {
        val current = mutableState.value
        mutableState.value = current.copy(week = current.week.copy(start = current.week.start.plusWeeks(offset)), selectedDay = current.selectedDay.plusWeeks(offset), doses = emptyList())
        refresh()
    }
    fun refresh() {
        request?.cancel()
        val week = mutableState.value.week
        mutableState.value = mutableState.value.copy(loading = true, error = null)
        request = viewModelScope.launch {
            when (val result = repository.getAgenda(olderAdultId, week.from, week.to)) {
                is AppResult.Success -> mutableState.value = mutableState.value.copy(doses = result.value, loading = false)
                is AppResult.Failure -> mutableState.value = mutableState.value.copy(loading = false, error = result.message)
            }
        }
    }
    class Factory(private val olderAdultId: String, private val repository: IntakeAgendaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = IntakeAgendaViewModel(olderAdultId, repository) as T
    }
}
