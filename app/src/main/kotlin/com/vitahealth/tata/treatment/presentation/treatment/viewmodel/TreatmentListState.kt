package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

/** Current treatment list, refresh status and recoverable load failure code. */
data class TreatmentListState(
    val treatments: List<TreatmentDetailReadModel> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)
