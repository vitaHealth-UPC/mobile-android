package com.vitahealth.tata.intake.presentation.agenda.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.queries.GetIntakeAgendaQuery
import com.vitahealth.tata.intake.application.queries.GetIntakeAgendaQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads one selected week and discards responses from superseded requests. */
class IntakeAgendaViewModel(
    private val olderAdultId: String,
    private val handler: GetIntakeAgendaQueryHandler,
) : ViewModel() {
    private val mutableState = MutableStateFlow(IntakeAgendaUiState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun selectDay(day: LocalDate) {
        if (day in mutableState.value.week.days)
            mutableState.value = mutableState.value.copy(selectedDay = day)
    }

    fun moveWeek(offset: Long) {
        val current = mutableState.value
        mutableState.value =
            current.copy(
                week = current.week.copy(start = current.week.start.plusWeeks(offset)),
                selectedDay = current.selectedDay.plusWeeks(offset),
                doses = emptyList(),
            )
        refresh()
    }

    fun refresh() {
        request?.cancel()
        val week = mutableState.value.week
        mutableState.value = mutableState.value.copy(loading = true, error = null)
        request =
            viewModelScope.launch {
                val result = handler(GetIntakeAgendaQuery(olderAdultId, week.from, week.to))
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(doses = result.value, loading = false)
                    is AppResult.Failure ->
                        mutableState.value =
                            mutableState.value.copy(loading = false, error = result.message)
                }
            }
    }

    /** Supplies the validated read-only agenda query handler. */
    class Factory(
        private val olderAdultId: String,
        private val handler: GetIntakeAgendaQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(IntakeAgendaViewModel::class.java))
            return IntakeAgendaViewModel(olderAdultId, handler) as T
        }
    }
}
