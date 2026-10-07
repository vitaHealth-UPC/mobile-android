package com.vitahealth.tata.intake.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.handlers.GetNextDoseQueryHandler
import com.vitahealth.tata.intake.application.queries.GetNextDoseQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.domain.model.DoseStatus
import java.time.LocalDate
import java.time.ZoneId

class NextDoseHomeViewModel(
    private val olderAdultId: String,
    private val olderAdultName: String,
    private val handler: GetNextDoseQueryHandler,
    private val agendaRepository: IntakeAgendaRepository? = null,
) : ViewModel() {
    private val _state = MutableStateFlow<NextDoseHomeUiState>(NextDoseHomeUiState.Loading)
    val state: StateFlow<NextDoseHomeUiState> = _state.asStateFlow()

    private var request: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        request?.cancel()
        _state.value = NextDoseHomeUiState.Loading
        request = viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val day = LocalDate.now(zone)
            val progress = when (val agenda = agendaRepository?.getAgenda(olderAdultId, day.atStartOfDay(zone).toInstant(), day.plusDays(1).atStartOfDay(zone).toInstant())) {
                is AppResult.Success -> {
                    val doses = agenda.value.filter { it.scheduledAt.atZone(zone).toLocalDate() == day }
                    DailyDoseProgress(doses.count { it.status == DoseStatus.CONFIRMED || it.status == DoseStatus.LATE }, doses.size)
                }
                else -> null
            }
            when (val result = handler(GetNextDoseQuery(olderAdultId))) {
                is AppResult.Success -> {
                    _state.value = result.value?.let {
                        NextDoseHomeUiState.NextDoseAvailable(
                            dose = it,
                            olderAdultName = olderAdultName,
                            progress = progress,
                        )
                    } ?: NextDoseHomeUiState.NoNextDose(olderAdultName, progress)
                }
                is AppResult.Failure -> {
                    _state.value = NextDoseHomeUiState.Error(
                        olderAdultName = olderAdultName,
                        message = result.message,
                    )
                }
            }
        }
    }

    class Factory(
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val handler: GetNextDoseQueryHandler,
        private val agendaRepository: IntakeAgendaRepository? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NextDoseHomeViewModel(
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                handler = handler,
                agendaRepository = agendaRepository,
            ) as T
    }
}
