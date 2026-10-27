package com.vitahealth.tata.analytics.presentation.history.ui

import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus

/** Formatted date and outcome of a recent medication intake. */
data class RecentIntakeUi(
    val whenLabel: String,
    val medicationName: String,
    val outcomeText: String,
    val status: IntakeOutcomeStatus,
)
