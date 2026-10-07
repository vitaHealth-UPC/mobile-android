package com.vitahealth.tata.analytics.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.analytics.application.handlers.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQuery
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.domain.model.AdherencePeriod
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

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
        loadJob = viewModelScope.launch {
            when (val result = handler(GetAdherenceSummaryQuery(olderAdultId, period.days))) {
                is AppResult.Success -> {
                    _state.value = result.value?.let {
                        AdherenceHistoryUiState.Content(it.toUi(), periodUpdated = changed)
                    } ?: if (changed) {
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

    class Factory(
        private val olderAdultId: String,
        private val handler: GetAdherenceSummaryQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AdherenceHistoryViewModel(
                olderAdultId = olderAdultId,
                handler = handler,
            ) as T
    }
}

private val trendDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-PE"))

private fun periodLabel(days: Int): String = "Últimos $days días"

private fun changeText(change: Int?, suffix: String = ""): String? =
    change?.let {
        val arrow = when {
            it > 0 -> "↑"
            it < 0 -> "↓"
            else -> "="
        }
        "$arrow ${abs(it)}%$suffix"
    }

private fun AdherenceSummaryReadModel.toUi(): AdherenceSummaryUi =
    AdherenceSummaryUi(
        periodLabel = periodLabel(periodDays),
        adherencePercent = adherencePercent,
        adherenceChangeText = changeText(adherenceChangePercent, " vs. $periodDays días previos"),
        onTimePercent = onTimePercent,
        onTimeChangeText = changeText(onTimeChangePercent),
        lateCount = lateCount,
        omittedCount = omittedCount,
        lateOmittedCaption = "últ. $periodDays días",
        trend = trend.map {
            AdherenceTrendPoint(
                label = trendDateFormatter.format(it.date).replace(".", ""),
                adherencePercent = it.adherencePercent,
            )
        },
    )
