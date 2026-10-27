package com.vitahealth.tata.monitoring.infrastructure.remote

import com.google.gson.Gson
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Response

class PersonalNoteResponseValidationTest {
    private val valid =
        PersonalNoteResponse(
            1,
            "Medicación",
            "Tomar con agua",
            PersonalNoteCategory.MEDICATION,
            "2026-10-22T08:00:00Z",
        )

    @Test
    fun malformedNotesDoNotBecomeDomainNotes() {
        for (body in
            listOf(
                valid.copy(id = 0),
                valid.copy(title = ""),
                valid.copy(text = " "),
                valid.copy(category = null),
                valid.copy(recordedAt = "invalid"),
            )) {
            assertThrows(RuntimeException::class.java) { body.toDomain() }
        }
        assertEquals(PersonalNoteCategory.MEDICATION, valid.toDomain().category)
    }

    @Test
    fun creationRejectsReceiptsForDifferentContent() = runBlocking {
        for (body in
            listOf(
                valid.copy(title = "Otro título"),
                valid.copy(text = "Otro contenido"),
                valid.copy(category = PersonalNoteCategory.ROUTINE),
            )) {
            val repository = repositoryReturning(body)
            val result = repository.create(valid.title, valid.text, valid.category!!)
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        }
    }

    @Test
    fun creationAcceptsServerWhitespaceNormalization() = runBlocking {
        val result =
            repositoryReturning(valid)
                .create("  ${valid.title}  ", "  ${valid.text}  ", valid.category!!)
        assertEquals(valid.toDomain(), (result as AppResult.Success).value)
    }

    private fun repositoryReturning(body: PersonalNoteResponse): RemotePersonalNotesRepository {
        val api =
            Proxy.newProxyInstance(
                PersonalNotesApiService::class.java.classLoader,
                arrayOf(PersonalNotesApiService::class.java),
            ) { _, method, _ ->
                if (method.name == "list") Response.success(listOf(body))
                else Response.success(body)
            } as PersonalNotesApiService
        return RemotePersonalNotesRepository(api)
    }

    @Test
    fun unknownServerCategoryIsRejectedByListAndCreate() = runBlocking {
        val body =
            Gson()
                .fromJson(
                    """{"id":1,"title":"Nota","text":"Texto","category":"UNKNOWN","recordedAt":"2026-10-22T08:00:00Z"}""",
                    PersonalNoteResponse::class.java,
                )
        val api =
            Proxy.newProxyInstance(
                PersonalNotesApiService::class.java.classLoader,
                arrayOf(PersonalNotesApiService::class.java),
            ) { _, method, _ ->
                if (method.name == "list") Response.success(listOf(body))
                else Response.success(body)
            } as PersonalNotesApiService
        val repository = RemotePersonalNotesRepository(api)

        assertEquals("INVALID_RESPONSE", (repository.list() as AppResult.Failure).code)
        assertEquals(
            "INVALID_RESPONSE",
            (repository.create("Nota", "Texto", PersonalNoteCategory.ROUTINE) as AppResult.Failure)
                .code,
        )
    }
}
