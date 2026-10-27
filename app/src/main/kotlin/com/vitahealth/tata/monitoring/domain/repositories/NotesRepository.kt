package com.vitahealth.tata.monitoring.domain.repositories

import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads and records caregiver follow-up notes without binding them to one alert. */
interface NotesRepository {
    /** Notes of the follow-up, most recent first as the backend returns them. */
    suspend fun notes(caregiverId: String, olderAdultId: String): AppResult<List<FollowUpNote>>

    suspend fun register(
        caregiverId: String,
        olderAdultId: String,
        text: String,
    ): AppResult<FollowUpNote>
}
