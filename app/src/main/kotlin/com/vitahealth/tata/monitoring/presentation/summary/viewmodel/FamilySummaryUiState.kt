package com.vitahealth.tata.monitoring.presentation.summary.viewmodel

import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary

/** Observable family summary and its currently open action dialog. */
data class FamilySummaryUiState(
    val loading: Boolean = true,
    val summary: FamilySummary? = null,
    val error: String? = null,
    val dialog: SummaryDialog? = null,
    val errorCode: String? = null,
)
