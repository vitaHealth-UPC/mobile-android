package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.handlers.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TreatmentDetailViewModel(
    private val caregiverId: String,
    private val olderAdultName: String,
    private val treatmentId: String,
    private val medicationLabelHint: String,
    private val handler: GetTreatmentDetailQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<TreatmentDetailUiState>(TreatmentDetailUiState.Loading)
    val state: StateFlow<TreatmentDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = TreatmentDetailUiState.Loading
        viewModelScope.launch {
            when (val result = handler(
                GetTreatmentDetailQuery(
                    caregiverId = caregiverId,
                    treatmentId = treatmentId,
                ),
            )) {
                is AppResult.Success -> {
                    _state.value = TreatmentDetailUiState.Loaded(
                        detail = result.value,
                        olderAdultName = olderAdultName,
                        medicationLabelHint = medicationLabelHint,
                    )
                }

                is AppResult.Failure -> {
                    _state.value = when (result.code) {
                        "CARE_LINK_NOT_AUTHORIZED" ->
                            TreatmentDetailUiState.Restricted(olderAdultName)
                        "RESOURCE_NOT_FOUND" ->
                            TreatmentDetailUiState.NotFound(olderAdultName)
                        else ->
                            TreatmentDetailUiState.Error(
                                olderAdultName = olderAdultName,
                                message = detailMessage(result),
                            )
                    }
                }
            }
        }
    }

    private fun detailMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            "INVALID_TREATMENT_STATE" -> "El tratamiento tiene un estado no reconocido."
            "INVALID_TREATMENT_REFERENCE" -> "No se pudo identificar el tratamiento."
            else -> failure.message
        }

    class Factory(
        private val caregiverId: String,
        private val olderAdultName: String,
        private val treatmentId: String,
        private val medicationLabelHint: String,
        private val handler: GetTreatmentDetailQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreatmentDetailViewModel(
                caregiverId = caregiverId,
                olderAdultName = olderAdultName,
                treatmentId = treatmentId,
                medicationLabelHint = medicationLabelHint,
                handler = handler,
            ) as T
    }
}
