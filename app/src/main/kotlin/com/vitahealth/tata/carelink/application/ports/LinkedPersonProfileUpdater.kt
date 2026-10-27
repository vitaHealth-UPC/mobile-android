package com.vitahealth.tata.carelink.application.ports

import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate

/** CareLink-owned authorized mutation; consumers never handle Retrofit or JPA. */
fun interface LinkedPersonProfileUpdater {
    suspend fun update(caregiverId: String, olderAdultId: String, name: String, birthDate: LocalDate):
        AppResult<Unit>
}
