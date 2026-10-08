package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.domain.model.Medication

/** What the banner under the list says. Each value maps to a string resource. */
enum class MedicationManagementMessage {
    Updated,
    Deactivated,
    ErrorRequiredFields,
    ErrorInactive,
    ErrorNotFound,
    ErrorOffline,
    ErrorGeneric,
}

/** The values being typed while a medication is edited. */
data class MedicationDraft(
    val medicationId: String,
    val name: String,
    val presentation: String,
)

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
        get() = message != null &&
            message != MedicationManagementMessage.Updated &&
            message != MedicationManagementMessage.Deactivated
}
