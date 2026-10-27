package com.vitahealth.tata.monitoring.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/** Retrieves and creates personal notes for the authenticated older-adult session. */
class RemotePersonalNotesRepository(private val api: PersonalNotesApiService) :
    PersonalNotesRepository {
    override suspend fun list() =
        request { api.list() }.map { it.map(PersonalNoteResponse::toDomain) }

    override suspend fun create(title: String, text: String, category: PersonalNoteCategory) =
        request { api.create(CreatePersonalNoteRequest(title, text, category)) }
            .map {
                val note = it.toDomain()
                require(
                    note.title == title.trim() &&
                        note.text == text.trim() &&
                        note.category == category
                )
                note
            }

    private suspend fun <T> request(call: suspend () -> Response<T>): AppResult<T> =
        try {
            val response = call()
            if (response.isSuccessful) AppResult.Success(requireNotNull(response.body()))
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
        } catch (e: MalformedJsonException) {
            AppResult.Failure("", e, "INVALID_RESPONSE")
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
