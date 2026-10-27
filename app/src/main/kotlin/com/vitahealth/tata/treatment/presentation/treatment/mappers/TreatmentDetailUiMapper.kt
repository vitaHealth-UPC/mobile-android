package com.vitahealth.tata.treatment.presentation.treatment.mappers

import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

internal fun medicationLabel(detail: TreatmentDetailReadModel, hint: String): String =
    hint.ifBlank { detail.medicationId ?: "Medicamento sin configurar" }

internal fun statusLabel(status: TreatmentStatus): String =
    when (status) {
        TreatmentStatus.DRAFT -> "Incompleto"
        TreatmentStatus.ACTIVE -> "Activo"
        TreatmentStatus.PAUSED -> "Pausado"
    }

internal fun firstNameDetail(fullName: String): String =
    fullName.trim().substringBefore(" ").ifBlank { "la persona vinculada" }
