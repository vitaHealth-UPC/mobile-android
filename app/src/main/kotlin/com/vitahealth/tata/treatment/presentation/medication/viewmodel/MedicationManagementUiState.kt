package com.vitahealth.tata.treatment.presentation.medication.viewmodel

import com.vitahealth.tata.treatment.domain.model.entities.Medication

/** Medication list, editable draft and in-flight mutation state for one adult. */
data class MedicationManagementUiState(
    val olderAdultName: String,
    val isLoading: Boolean = true,
    val medications: List<Medication> = emptyList(),
    /** True when the caregiver has no active care link with the older adult. */
    val accessDenied: Boolean = false,
    val loadFailed: Boolean = false,
    val draft: MedicationDraft? = null,
    val deactivating: Medication? = null,
    val isSaving: Boolean = false,
    val message: MedicationManagementMessage? = null,
) {
    val messageIsError: Boolean
        get() =
            message != null &&
                message != MedicationManagementMessage.Updated &&
                message != MedicationManagementMessage.Deactivated
}
