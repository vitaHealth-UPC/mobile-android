package com.vitahealth.tata.intake.presentation.detail

import com.vitahealth.tata.intake.application.handlers.ConfirmDoseCommandHandler
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.domain.model.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.DoseStatus
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
    private val confirmHandler: ConfirmDoseCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<DoseDetailUiState>(DoseDetailUiState.Loading)
    val state: StateFlow<DoseDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun confirm() {
        val content = _state.value as? DoseDetailUiState.Content ?: return
        if (content.confirming || content.dose.status != DoseStatus.PENDING) return
        _state.value = content.copy(confirming = true, confirmationMessage = null)
        viewModelScope.launch {
            _state.value = when (val result = confirmHandler(ConfirmDoseCommand(intakeId, ConfirmationChannel.TOUCH))) {
                is AppResult.Success -> DoseDetailUiState.Content(result.value, confirmationSucceeded = true)
                is AppResult.Failure -> content.copy(confirmationMessage = result.message)
            }
        }
    }

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
        private val confirmHandler: ConfirmDoseCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DoseDetailViewModel(
                intakeId = intakeId,
                handler = handler,
                confirmHandler = confirmHandler,
            ) as T
    }
}
