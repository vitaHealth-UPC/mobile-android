package com.vitahealth.tata.monitoring.application

import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.shared.common.result.AppResult

interface NotesRepository {
    /** Notes of the follow-up, most recent first as the backend returns them. */
    suspend fun notes(caregiverId: String, olderAdultId: String): AppResult<List<FollowUpNote>>
    suspend fun register(caregiverId: String, olderAdultId: String, text: String): AppResult<FollowUpNote>
}
