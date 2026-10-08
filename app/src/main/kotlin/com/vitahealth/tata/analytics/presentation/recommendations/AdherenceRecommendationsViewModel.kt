package com.vitahealth.tata.analytics.presentation.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.analytics.application.handlers.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdherenceRecommendationsViewModel(
    private val olderAdultId: String,
    private val handler: GetAdherenceRecommendationsQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<AdherenceRecommendationsUiState>(AdherenceRecommendationsUiState.Loading)
    val state: StateFlow<AdherenceRecommendationsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = AdherenceRecommendationsUiState.Loading
        viewModelScope.launch {
            when (val result = handler(GetAdherenceRecommendationsQuery(olderAdultId, RECOMMENDATIONS_PERIOD_DAYS))) {
                is AppResult.Success -> {
                    _state.value = result.value?.let { AdherenceRecommendationsUiState.Content(it) }
                        ?: AdherenceRecommendationsUiState.InsufficientEvidence
                }
                is AppResult.Failure -> {
                    _state.value = AdherenceRecommendationsUiState.Error(result.message)
                }
            }
        }
    }

    class Factory(
        private val olderAdultId: String,
        private val handler: GetAdherenceRecommendationsQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AdherenceRecommendationsViewModel(
                olderAdultId = olderAdultId,
                handler = handler,
            ) as T
    }
}
