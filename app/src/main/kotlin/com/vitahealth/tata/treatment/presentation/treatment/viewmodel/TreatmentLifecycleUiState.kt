package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/** Reviewed regimen, current lifecycle state and status of the pending operation. */
data class TreatmentLifecycleUiState(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val medicationId: String,
    val medicationLabel: String,
    val treatmentId: String,
    val treatmentName: String,
    val dosage: String,
    val frequency: String,
    val scheduleText: String,
    val instructions: String,
    val reminderDelayMinutes: Int,
    val status: TreatmentStatus = TreatmentStatus.DRAFT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeMedicationCount: Int? = null,
    val isRefreshingManagement: Boolean = false,
    val managementReadFailed: Boolean = false,
    val usesDefaultFollowUp: Boolean = false,
    val adherence: TreatmentAdherenceReadModel? = null,
    val adherenceError: String? = null,
    val regimenSaved: Boolean = false,
    val configurationIncomplete: Boolean = false,
    val medicationInactive: Boolean = false,
)
