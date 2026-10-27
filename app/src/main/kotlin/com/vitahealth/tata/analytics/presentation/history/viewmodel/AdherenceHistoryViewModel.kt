package com.vitahealth.tata.analytics.presentation.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQuery
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod
import com.vitahealth.tata.analytics.presentation.history.mappers.toUi
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads the selected adherence period and publishes its history presentation. */
class AdherenceHistoryViewModel(
    private val olderAdultId: String,
    private val handler: GetAdherenceSummaryQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<AdherenceHistoryUiState>(AdherenceHistoryUiState.Loading)
    val state: StateFlow<AdherenceHistoryUiState> = _state.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(AdherencePeriod.LastMonth)
    val selectedPeriod: StateFlow<AdherencePeriod> = _selectedPeriod.asStateFlow()

    private var periodChanged = false
    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun selectPeriod(period: AdherencePeriod) {
        if (period == _selectedPeriod.value) return
        _selectedPeriod.value = period
        periodChanged = true
        load()
    }

    private fun load() {
        val period = _selectedPeriod.value
        val changed = periodChanged
        _state.value = AdherenceHistoryUiState.Loading
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                val result = handler(GetAdherenceSummaryQuery(olderAdultId, period.days))
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> {
                        _state.value =
                            result.value?.let {
                                AdherenceHistoryUiState.Content(
                                    it.toUi(LocalDate.now()),
                                    periodUpdated = changed,
                                )
                            }
                                ?: if (changed) {
                                    AdherenceHistoryUiState.NoResults(period.label())
                                } else {
                                    AdherenceHistoryUiState.InsufficientData(period.label())
                                }
                    }
                    is AppResult.Failure -> {
                        _state.value = AdherenceHistoryUiState.Error(result.message)
                    }
                }
            }
    }

    /** Supplies the adult-specific query handler for this history screen. */
    class Factory(
        private val olderAdultId: String,
        private val handler: GetAdherenceSummaryQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AdherenceHistoryViewModel::class.java))
            return AdherenceHistoryViewModel(olderAdultId = olderAdultId, handler = handler) as T
        }
    }
}
