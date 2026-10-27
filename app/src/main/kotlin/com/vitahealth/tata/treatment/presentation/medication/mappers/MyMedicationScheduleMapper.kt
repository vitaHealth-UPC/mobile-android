package com.vitahealth.tata.treatment.presentation.medication.mappers

import com.vitahealth.tata.treatment.application.readmodels.MyMedication

internal fun scheduleLabel(medication: MyMedication): String =
    medication.instructions.ifBlank { medication.scheduledTimes.joinToString(" · ") }
