package com.vitahealth.tata.monitoring.presentation.summary.viewmodel

import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel

/** Content and loading state of a summary action dialog. */
data class SummaryDialog(
    val title: String,
    val rows: List<String> = emptyList(),
    val loading: Boolean = false,
    val contact: ContactChannel? = null,
)
