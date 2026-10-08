package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.PersonalNotesRepository
import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import retrofit2.http.*

data class PersonalNoteResponse(
    val id: Long,
    val title: String,
    val text: String,
    val category: PersonalNoteCategory,
    val recordedAt: String,
) {
    fun toDomain() = PersonalNote(id, title, text, category, Instant.parse(recordedAt))
}

data class CreatePersonalNoteRequest(
    val title: String,
    val text: String,
    val category: PersonalNoteCategory,
)

interface PersonalNotesApiService {
    @GET("api/v1/me/notes") suspend fun list(): Response<List<PersonalNoteResponse>>

    @POST("api/v1/me/notes")
    suspend fun create(@Body request: CreatePersonalNoteRequest): Response<PersonalNoteResponse>
}

class RemotePersonalNotesRepository(private val api: PersonalNotesApiService) :
    PersonalNotesRepository {
    override suspend fun list() =
        request { api.list() }.map { it.map(PersonalNoteResponse::toDomain) }

    override suspend fun create(title: String, text: String, category: PersonalNoteCategory) =
        request { api.create(CreatePersonalNoteRequest(title, text, category)) }
            .map { it.toDomain() }

    private suspend fun <T> request(call: suspend () -> Response<T>): AppResult<T> =
        try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) AppResult.Success(body)
            else
                AppResult.Failure(
                    "",
                    code =
                        when (response.code()) {
                            401 -> "SESSION_REQUIRED"
                            403 -> "OLDER_ADULT_SESSION_REQUIRED"
                            else -> "REQUEST_FAILED"
                        },
                )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            AppResult.Failure("", e, "NETWORK_UNAVAILABLE")
        } catch (e: RuntimeException) {
            AppResult.Failure("", e, "INVALID_RESPONSE")
        }

    private inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
        when (this) {
            is AppResult.Success ->
                try {
                    AppResult.Success(transform(value))
                } catch (e: RuntimeException) {
                    AppResult.Failure("", e, "INVALID_RESPONSE")
                }
            is AppResult.Failure -> this
        }
}
