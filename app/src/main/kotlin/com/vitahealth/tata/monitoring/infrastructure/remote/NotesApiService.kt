package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.OffsetDateTime

/** `CaregiverNoteResource`; nullable for the same reason as [AlertSummaryResponse]. */
data class CaregiverNoteResponse(
    val id: Long?,
    val text: String?,
    val recordedAt: String?,
    val familiarId: String?,
)

/** `CreateCaregiverNoteResource`: the author goes in the body, there is no `caregiverId` query on POST. */
data class CreateCaregiverNoteRequest(val familiarId: String, val text: String)

interface NotesApiService {
    @GET("api/v1/older-adults/{olderAdultId}/notes")
    suspend fun notes(
        @Path("olderAdultId") olderAdultId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<List<CaregiverNoteResponse>>

    @POST("api/v1/older-adults/{olderAdultId}/notes")
    suspend fun register(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: CreateCaregiverNoteRequest,
    ): Response<CaregiverNoteResponse>
}

fun CaregiverNoteResponse.toDomain(): FollowUpNote = FollowUpNote(
    id = requireNotNull(id) { "note id is missing" },
    text = text.orEmpty(),
    recordedAt = try {
        OffsetDateTime.parse(requireNotNull(recordedAt) { "note date is missing" }).toInstant()
    } catch (exception: java.time.DateTimeException) {
        throw IllegalArgumentException("invalid note date: $recordedAt", exception)
    },
    familiarId = familiarId.orEmpty(),
)
