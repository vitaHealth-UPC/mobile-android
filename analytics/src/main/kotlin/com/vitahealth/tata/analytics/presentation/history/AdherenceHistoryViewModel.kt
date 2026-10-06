package com.vitahealth.tata.analytics.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.analytics.application.handlers.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQuery
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private const val DEFAULT_PERIOD_DAYS = 30

class AdherenceHistoryViewModel(
    private val olderAdultId: String,
    private val handler: GetAdherenceSummaryQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<AdherenceHistoryUiState>(AdherenceHistoryUiState.Loading)
    val state: StateFlow<AdherenceHistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = AdherenceHistoryUiState.Loading
        viewModelScope.launch {
            when (val result = handler(GetAdherenceSummaryQuery(olderAdultId, DEFAULT_PERIOD_DAYS))) {
                is AppResult.Success -> {
                    _state.value = result.value?.let { AdherenceHistoryUiState.Content(it.toUi()) }
                        ?: AdherenceHistoryUiState.InsufficientData(periodLabel(DEFAULT_PERIOD_DAYS))
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
