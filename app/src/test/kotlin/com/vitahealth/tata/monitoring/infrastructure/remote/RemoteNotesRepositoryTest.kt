package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.Instant

class RemoteNotesRepositoryTest {
    private val calls = mutableListOf<List<Any?>>()

    /** Values of the `CaregiverNoteResource` example published in the backend Swagger. */
    private val swaggerNote = CaregiverNoteResponse(
        id = 1,
        text = "I called her and she had already taken the pill.",
        recordedAt = "2026-10-05T14:10:00Z",
        familiarId = "1",
    )

    private fun api(
        notes: () -> Response<List<CaregiverNoteResponse>> = { error("unexpected list") },
        register: () -> Response<CaregiverNoteResponse> = { error("unexpected register") },
    ) = object : NotesApiService {
        override suspend fun notes(olderAdultId: String, caregiverId: String): Response<List<CaregiverNoteResponse>> {
            calls += listOf("notes", olderAdultId, caregiverId)
            return notes()
        }

        override suspend fun register(olderAdultId: String, request: CreateCaregiverNoteRequest): Response<CaregiverNoteResponse> {
            calls += listOf("register", olderAdultId, request)
            return register()
        }
    }

    private fun <T> httpError(code: Int): Response<T> = Response.error(code, """{"code":"X","message":"m"}""".toResponseBody())

    private fun failureCode(result: AppResult<*>) = (result as AppResult.Failure).code

    @Test fun mapsTheSwaggerNote() {
        val note = swaggerNote.toDomain()

        assertEquals(1L, note.id)
        assertEquals("I called her and she had already taken the pill.", note.text)
        assertEquals(Instant.parse("2026-10-05T14:10:00Z"), note.recordedAt)
        assertEquals("1", note.familiarId)
    }

    @Test fun listsTheNotesWithTheCaregiverQuery() = runBlocking {
        val result = RemoteNotesRepository(api(notes = { Response.success(listOf(swaggerNote)) })).notes("caregiver-1", "adult-1")

        assertEquals(listOf(1L), (result as AppResult.Success).value.map { it.id })
        assertEquals(listOf("notes", "adult-1", "caregiver-1"), calls.single())
    }

    @Test fun registersTheNoteWithTheCaregiverAsAuthor() = runBlocking {
        val result = RemoteNotesRepository(api(register = { Response.success(201, swaggerNote) })).register("caregiver-1", "adult-1", "Called her")

        assertEquals(1L, (result as AppResult.Success).value.id)
        assertEquals(listOf("register", "adult-1", CreateCaregiverNoteRequest(familiarId = "caregiver-1", text = "Called her")), calls.single())
    }

    @Test fun mapsTheNoteFailures() = runBlocking {
        mapOf(400 to AlertFailureCodes.NOTE_REJECTED, 403 to AlertFailureCodes.ACCESS_DENIED, 404 to AlertFailureCodes.NOT_FOUND).forEach { (status, code) ->
            val result = RemoteNotesRepository(api(register = { httpError(status) })).register("c", "a", "text")
            assertEquals("HTTP $status", code, failureCode(result))
        }
        assertEquals(AlertFailureCodes.NOT_FOUND, failureCode(RemoteNotesRepository(api(notes = { httpError(404) })).notes("c", "a")))
    }

    @Test fun mapsMissingConnectionToNetwork() = runBlocking {
        val result = RemoteNotesRepository(api(notes = { throw IOException("offline") })).notes("c", "a")

        assertEquals(AlertFailureCodes.NETWORK, failureCode(result))
    }

    @Test fun rejectsANoteWithoutId() = runBlocking {
        val result = RemoteNotesRepository(api(notes = { Response.success(listOf(swaggerNote.copy(id = null))) })).notes("c", "a")

        assertEquals(AlertFailureCodes.INVALID_RESPONSE, failureCode(result))
    }
}
