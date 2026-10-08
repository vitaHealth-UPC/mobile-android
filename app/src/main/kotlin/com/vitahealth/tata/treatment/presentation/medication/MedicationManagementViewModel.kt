package com.vitahealth.tata.treatment.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.treatment.application.commands.DeactivateMedicationCommand
import com.vitahealth.tata.treatment.application.commands.UpdateMedicationCommand
import com.vitahealth.tata.treatment.application.handlers.DeactivateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.handlers.UpdateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQuery
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicationManagementViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    olderAdultName: String,
    private val listMedications: ListMedicationsQueryHandler,
    private val updateMedication: UpdateMedicationCommandHandler,
    private val deactivateMedication: DeactivateMedicationCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(MedicationManagementUiState(olderAdultName = olderAdultName))
    val state: StateFlow<MedicationManagementUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, loadFailed = false, accessDenied = false, message = null) }
        viewModelScope.launch {
            when (val result = listMedications(ListMedicationsQuery(caregiverId, olderAdultId))) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, medications = result.value)
                }

                is AppResult.Failure -> _state.update {
                    if (result.code == "CARE_LINK_NOT_AUTHORIZED") {
                        it.copy(isLoading = false, accessDenied = true)
                    } else {
                        it.copy(isLoading = false, loadFailed = true, message = errorMessage(result))
                    }
                }
            }
        }
    }

    fun onEdit(medication: Medication) {
        if (!medication.active || _state.value.isSaving) return
        _state.update {
            it.copy(
                draft = MedicationDraft(medication.id, medication.name, medication.presentation),
                message = null,
            )
        }
    }

    fun onDraftNameChange(value: String) = _state.update {
        it.copy(draft = it.draft?.copy(name = value), message = null)
    }

    fun onDraftPresentationChange(value: String) = _state.update {
        it.copy(draft = it.draft?.copy(presentation = value), message = null)
    }

    fun onCancelEdit() = _state.update { it.copy(draft = null, message = null) }

    fun onSaveEdit() {
        val current = _state.value
        val draft = current.draft ?: return
        if (current.isSaving) return

        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val command = UpdateMedicationCommand(caregiverId, draft.medicationId, draft.name, draft.presentation)
            when (val result = updateMedication(command)) {
                is AppResult.Success -> _state.update { state ->
                    state.copy(
                        isSaving = false,
                        draft = null,
                        medications = state.medications.replacing(result.value),
                        message = MedicationManagementMessage.Updated,
                    )
                }

                // The draft stays open so the caregiver can correct it.
                is AppResult.Failure -> _state.update {
                    it.copy(isSaving = false, message = errorMessage(result))
                }
            }
        }
    }

    fun onDeactivateRequest(medication: Medication) {
        if (!medication.active || _state.value.isSaving) return
        _state.update { it.copy(deactivating = medication, message = null) }
    }

    fun onDeactivateCancel() = _state.update { it.copy(deactivating = null) }

    fun onDeactivateConfirm() {
        val current = _state.value
        val medication = current.deactivating ?: return
        if (current.isSaving) return

        _state.update { it.copy(isSaving = true, deactivating = null, message = null) }
        viewModelScope.launch {
            when (val result = deactivateMedication(DeactivateMedicationCommand(caregiverId, medication.id))) {
                is AppResult.Success -> _state.update { state ->
                    state.copy(
                        isSaving = false,
                        draft = state.draft?.takeIf { it.medicationId != medication.id },
                        medications = state.medications.replacing(result.value),
                        message = MedicationManagementMessage.Deactivated,
                    )
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isSaving = false, message = errorMessage(result))
                }
            }
        }
    }

    private fun List<Medication>.replacing(updated: Medication): List<Medication> =
        map { if (it.id == updated.id) updated else it }

    private fun errorMessage(failure: AppResult.Failure): MedicationManagementMessage =
        when (failure.code) {
            "REQUIRED_FIELDS_MISSING", "REQUEST_VALIDATION_FAILED" -> MedicationManagementMessage.ErrorRequiredFields
            "CONFLICT" -> MedicationManagementMessage.ErrorInactive
            "RESOURCE_NOT_FOUND" -> MedicationManagementMessage.ErrorNotFound
            "NETWORK_UNAVAILABLE" -> MedicationManagementMessage.ErrorOffline
            else -> MedicationManagementMessage.ErrorGeneric
        }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val listMedications: ListMedicationsQueryHandler,
        private val updateMedication: UpdateMedicationCommandHandler,
        private val deactivateMedication: DeactivateMedicationCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedicationManagementViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                listMedications = listMedications,
                updateMedication = updateMedication,
                deactivateMedication = deactivateMedication,
            ) as T
    }
}
