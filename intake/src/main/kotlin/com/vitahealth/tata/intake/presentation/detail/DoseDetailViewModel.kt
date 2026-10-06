package com.vitahealth.tata.intake.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.handlers.GetDoseDetailQueryHandler
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DoseDetailViewModel(
    private val intakeId: String,
    private val handler: GetDoseDetailQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<DoseDetailUiState>(DoseDetailUiState.Loading)
    val state: StateFlow<DoseDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = DoseDetailUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = handler(GetDoseDetailQuery(intakeId))) {
                is AppResult.Success -> DoseDetailUiState.Content(result.value)
                is AppResult.Failure -> DoseDetailUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val intakeId: String,
        private val handler: GetDoseDetailQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DoseDetailViewModel(
                intakeId = intakeId,
                handler = handler,
            ) as T
    }
}
