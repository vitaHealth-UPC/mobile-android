package com.vitahealth.tata.analytics.application.readmodels

import java.time.LocalDate

/** Adherence percentage associated with one calendar date. */
data class AdherenceTrendPointReadModel(val date: LocalDate, val adherencePercent: Int)
