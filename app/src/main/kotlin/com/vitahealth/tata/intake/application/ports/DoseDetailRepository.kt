package com.vitahealth.tata.intake.application.ports

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads the detail needed to display and confirm one scheduled intake. */
interface DoseDetailRepository {
    suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel>
}
