package com.vitahealth.tata.treatment.presentation.medication.viewmodel

import com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose
import com.vitahealth.tata.treatment.application.readmodels.MyMedication

/** Catalog content, next scheduled intake, selected filter and current load status. */
data class MyMedicationsUiState(
    val medications: List<MyMedication> = emptyList(),
    val nextDose: MedicationNextDose? = null,
    val loading: Boolean = true,
    val errorCode: String? = null,
    val history: Boolean = false,
)
