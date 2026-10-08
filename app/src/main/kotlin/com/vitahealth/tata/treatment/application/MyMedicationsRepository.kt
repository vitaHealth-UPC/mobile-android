package com.vitahealth.tata.treatment.application

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.MyMedication

/** Catalog owned by the authenticated older adult; no client-supplied owner is accepted. */
interface MyMedicationsRepository {
    suspend fun list(): AppResult<List<MyMedication>>
}
