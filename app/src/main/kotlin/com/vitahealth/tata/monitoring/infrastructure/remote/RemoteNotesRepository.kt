package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.NotesRepository
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteNotesRepository(private val api: NotesApiService) : NotesRepository {

    override suspend fun notes(caregiverId: String, olderAdultId: String): AppResult<List<FollowUpNote>> = guardedFollowUp {
        val response = api.notes(olderAdultId, caregiverId)
        val body = response.body()
        followUpHttpFailure(response) ?: if (body == null) {
            followUpFailure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            AppResult.Success(body.map { it.toDomain() })
        }
    }

    /** The caregiver is the author: `familiarId` takes the caregiver id. */
    override suspend fun register(caregiverId: String, olderAdultId: String, text: String): AppResult<FollowUpNote> = guardedFollowUp {
        val response = api.register(olderAdultId, CreateCaregiverNoteRequest(familiarId = caregiverId, text = text))
        val body = response.body()
        followUpHttpFailure(response, badRequestCode = AlertFailureCodes.NOTE_REJECTED) ?: if (body == null) {
            followUpFailure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            AppResult.Success(body.toDomain())
        }
    }
}
