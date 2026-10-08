package com.vitahealth.tata.app

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vitahealth.tata.monitoring.infrastructure.remote.CaregiverNoteResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.CreateCaregiverNoteRequest
import com.vitahealth.tata.monitoring.infrastructure.remote.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Payloads of the notes endpoints as the backend Swagger publishes them, parsed with the app's Gson. */
class NotesContractTest {
    private val gson = Gson()

    private val noteJson = """
        {
          "id": 1,
          "text": "I called her and she had already taken the pill.",
          "recordedAt": "2026-10-05T14:10:00Z",
          "familiarId": "1"
        }
    """.trimIndent()

    @Test fun noteListMapsToTheDomain() {
        val type = object : TypeToken<List<CaregiverNoteResponse>>() {}.type
        val notes = gson.fromJson<List<CaregiverNoteResponse>>("[$noteJson]", type).map { it.toDomain() }

        assertEquals(1L, notes.single().id)
        assertEquals(Instant.parse("2026-10-05T14:10:00Z"), notes.single().recordedAt)
        assertEquals("1", notes.single().familiarId)
    }

    @Test fun emptyListIsValid() {
        val type = object : TypeToken<List<CaregiverNoteResponse>>() {}.type

        assertTrue(gson.fromJson<List<CaregiverNoteResponse>>("[]", type).isEmpty())
    }

    @Test fun createRequestMatchesCreateCaregiverNoteResource() {
        val json = gson.toJson(CreateCaregiverNoteRequest(familiarId = "00000000-0000-0000-0000-000000000001", text = "Called her"))

        assertEquals("""{"familiarId":"00000000-0000-0000-0000-000000000001","text":"Called her"}""", json)
    }
}
