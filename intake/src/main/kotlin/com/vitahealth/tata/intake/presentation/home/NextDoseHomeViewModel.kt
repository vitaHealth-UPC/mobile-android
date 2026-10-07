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

class NextDoseHomeViewModel(
    private val olderAdultId: String,
    private val olderAdultName: String,
    private val handler: GetNextDoseQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<NextDoseHomeUiState>(NextDoseHomeUiState.Loading)
    val state: StateFlow<NextDoseHomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = NextDoseHomeUiState.Loading
        viewModelScope.launch {
            when (val result = handler(GetNextDoseQuery(olderAdultId))) {
                is AppResult.Success -> {
                    _state.value = result.value?.let {
                        NextDoseHomeUiState.NextDoseAvailable(
                            dose = it,
                            olderAdultName = olderAdultName,
                        )
                    } ?: NextDoseHomeUiState.NoNextDose(olderAdultName)
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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NextDoseHomeViewModel(
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                handler = handler,
            ) as T
    }
}
