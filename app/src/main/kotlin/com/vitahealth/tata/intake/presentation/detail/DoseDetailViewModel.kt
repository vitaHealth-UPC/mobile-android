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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** Code the intake repository gives a 409 from the confirmation endpoint. */
private const val NOT_CONFIRMABLE = "INTAKE_NOT_CONFIRMABLE"

class DoseDetailViewModel(
    private val intakeId: String,
    private val handler: GetDoseDetailQueryHandler,
    private val confirmHandler: ConfirmDoseCommandHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
) : ViewModel() {
    private val _state = MutableStateFlow<DoseDetailUiState>(DoseDetailUiState.Loading)
    val state: StateFlow<DoseDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun confirm() {
        val content = _state.value as? DoseDetailUiState.Content ?: return
        if (content.confirming || content.confirmationUnavailable || content.dose.status != DoseStatus.PENDING) return
        _state.value = content.copy(confirming = true, confirmationMessage = null)
        viewModelScope.launch(context) {
            _state.value = when (val result = confirmHandler(ConfirmDoseCommand(intakeId, ConfirmationChannel.TOUCH))) {
                is AppResult.Success -> DoseDetailUiState.Content(
                    result.value,
                    confirmationSucceeded = true,
                    outcome = if (result.value.status == DoseStatus.LATE) ConfirmationOutcome.LATE else ConfirmationOutcome.CONFIRMED,
                )
                is AppResult.Failure -> if (result.code == NOT_CONFIRMABLE) refreshRejectedConfirmation(content) else content.copy(confirmationMessage = result.message)
            }
        }
    }

    /**
     * A conflict says the intake cannot be confirmed. Read its persisted status before showing an
     * outcome; another device may have confirmed it, and a failed read cannot prove an omission.
     */
    private suspend fun refreshRejectedConfirmation(content: DoseDetailUiState.Content): DoseDetailUiState.Content =
        when (val result = handler(GetDoseDetailQuery(intakeId))) {
            is AppResult.Success -> DoseDetailUiState.Content(
                dose = result.value,
                outcome = if (result.value.status == DoseStatus.OMITTED) ConfirmationOutcome.OMISSION_PRESERVED else null,
                confirmationUnavailable = result.value.status == DoseStatus.PENDING,
            )
            is AppResult.Failure -> content.copy(
                confirming = false,
                confirmationUnavailable = true,
                confirmationMessage = result.message,
            )
        }

    private fun load() {
        _state.value = DoseDetailUiState.Loading
        viewModelScope.launch(context) {
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
