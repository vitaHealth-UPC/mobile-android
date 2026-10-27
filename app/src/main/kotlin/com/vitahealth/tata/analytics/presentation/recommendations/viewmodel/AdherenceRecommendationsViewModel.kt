package com.vitahealth.tata.analytics.presentation.recommendations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQuery
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.presentation.recommendations.mappers.toUi
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Presents recommendations from the latest requested analytics load. */
class AdherenceRecommendationsViewModel(
    private val olderAdultId: String,
    private val handler: GetAdherenceRecommendationsQueryHandler,
) : ViewModel() {
    private val _state =
        MutableStateFlow<AdherenceRecommendationsUiState>(AdherenceRecommendationsUiState.Loading)
    val state: StateFlow<AdherenceRecommendationsUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        _state.value = AdherenceRecommendationsUiState.Loading
        loadJob =
            viewModelScope.launch {
                val result =
                    handler(
                        GetAdherenceRecommendationsQuery(olderAdultId, RECOMMENDATIONS_PERIOD_DAYS)
                    )
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> {
                        _state.value =
                            result.value?.let { AdherenceRecommendationsUiState.Content(it.toUi()) }
                                ?: AdherenceRecommendationsUiState.InsufficientEvidence
                    }
                    is AppResult.Failure -> {
                        _state.value = AdherenceRecommendationsUiState.Error(result.message)
                    }
                }
            }
    }

    /** Creates the screen ViewModel with the selected adult and query handler. */
    class Factory(
        private val olderAdultId: String,
        private val handler: GetAdherenceRecommendationsQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AdherenceRecommendationsViewModel::class.java))
            return AdherenceRecommendationsViewModel(olderAdultId = olderAdultId, handler = handler)
                as T
        }
    }
}
