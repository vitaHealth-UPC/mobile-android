package com.vitahealth.tata.treatment.application.queries

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MyMedicationsOverviewReader
import com.vitahealth.tata.treatment.application.ports.MyMedicationsRepository
import com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose
import com.vitahealth.tata.treatment.application.readmodels.MyMedicationsOverview
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Combines both reads while preserving failures and cancellation between operations. */
class GetMyMedicationsOverviewQueryHandler(
    private val catalog: MyMedicationsRepository,
    private val nextDose: suspend () -> AppResult<MedicationNextDose?>,
) : MyMedicationsOverviewReader {
    override suspend fun read(): AppResult<MyMedicationsOverview> {
        val medications =
            when (val result = catalog.list()) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> return result
            }
        currentCoroutineContext().ensureActive()
        val dose = nextDose()
        currentCoroutineContext().ensureActive()
        return when (dose) {
            is AppResult.Success ->
                AppResult.Success(MyMedicationsOverview(medications, dose.value))
            is AppResult.Failure -> dose
        }
    }
}
