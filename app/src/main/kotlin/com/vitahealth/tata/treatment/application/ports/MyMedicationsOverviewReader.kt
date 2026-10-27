package com.vitahealth.tata.treatment.application.ports

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.MyMedicationsOverview

/** Reads the adult catalog and next intake as one coherent operation. */
fun interface MyMedicationsOverviewReader {
    suspend fun read(): AppResult<MyMedicationsOverview>
}
