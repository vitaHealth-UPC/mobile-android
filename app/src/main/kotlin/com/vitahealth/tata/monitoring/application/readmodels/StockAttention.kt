package com.vitahealth.tata.monitoring.application.readmodels

/** Remaining medication stock requiring caregiver attention. */
data class StockAttention(val medicationId: String, val medicationName: String, val remaining: Int)
