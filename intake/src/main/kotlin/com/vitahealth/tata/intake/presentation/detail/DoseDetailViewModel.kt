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
        if (content.confirming || content.dose.status != DoseStatus.PENDING) return
        _state.value = content.copy(confirming = true, confirmationMessage = null)
        viewModelScope.launch(context) {
            _state.value = when (val result = confirmHandler(ConfirmDoseCommand(intakeId, ConfirmationChannel.TOUCH))) {
                is AppResult.Success -> DoseDetailUiState.Content(
                    result.value,
                    confirmationSucceeded = true,
                    outcome = if (result.value.status == DoseStatus.LATE) ConfirmationOutcome.LATE else ConfirmationOutcome.CONFIRMED,
                )
                is AppResult.Failure -> if (result.code == NOT_CONFIRMABLE) omissionPreserved(content) else content.copy(confirmationMessage = result.message)
            }
        }
    }

    /**
     * 409: the backend already registered the omission and keeps it. The dose is read again so the screen
     * shows the stored outcome instead of the pending one it had.
     */
    private suspend fun omissionPreserved(content: DoseDetailUiState.Content): DoseDetailUiState.Content {
        val dose = (handler(GetDoseDetailQuery(intakeId)) as? AppResult.Success)?.value ?: content.dose.copy(status = DoseStatus.OMITTED)
        return DoseDetailUiState.Content(dose, outcome = ConfirmationOutcome.OMISSION_PRESERVED)
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
