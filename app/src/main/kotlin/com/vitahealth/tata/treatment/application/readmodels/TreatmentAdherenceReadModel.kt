package com.vitahealth.tata.treatment.application.readmodels

import java.time.LocalDate

/** Regimen-scoped evidence; missing dates have no definitive observations. */
data class TreatmentAdherenceReadModel(val percent: Int, val trend: List<TreatmentAdherencePoint>)

data class TreatmentAdherencePoint(val date: LocalDate, val percent: Int)
