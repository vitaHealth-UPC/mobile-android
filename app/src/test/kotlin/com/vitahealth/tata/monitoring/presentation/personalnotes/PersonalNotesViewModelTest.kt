package com.vitahealth.tata.monitoring.presentation.personalnotes

import com.vitahealth.tata.monitoring.application.PersonalNotesRepository
import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalNotesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun failedSaveKeepsComposerOpenAndDoesNotInventSavedNote() =
        runTest(dispatcher) {
            val repository =
                object : PersonalNotesRepository {
                    override suspend fun list() = AppResult.Success(emptyList<PersonalNote>())

                    override suspend fun create(
                        title: String,
                        text: String,
                        category: PersonalNoteCategory,
                    ) = AppResult.Failure("", code = "NETWORK_UNAVAILABLE")
                }
            val model = PersonalNotesViewModel(repository)
            advanceUntilIdle()
            model.compose(true)
            model.save("Agua", "Preparar vaso", PersonalNoteCategory.ROUTINE)
            advanceUntilIdle()
            assertTrue(model.state.value.composing)
            assertTrue(model.state.value.saveError)
            assertFalse(model.state.value.saving)
            assertTrue(model.state.value.notes.isEmpty())
        }

    @Test
    fun doubleTapWritesOnlyOnceAndDisplaysServerNote() =
        runTest(dispatcher) {
            var calls = 0
            val note =
                PersonalNote(
                    7,
                    "Agua",
                    "Preparar vaso",
                    PersonalNoteCategory.MEDICATION,
                    Instant.EPOCH,
                )
            val repository =
                object : PersonalNotesRepository {
                    override suspend fun list() = AppResult.Success(emptyList<PersonalNote>())

                    override suspend fun create(
                        title: String,
                        text: String,
                        category: PersonalNoteCategory,
                    ): AppResult<PersonalNote> {
                        calls++
                        assertEquals("Agua", title)
                        return AppResult.Success(note)
                    }
                }
            val model = PersonalNotesViewModel(repository)
            advanceUntilIdle()
            model.compose(true)
            repeat(2) { model.save(" Agua ", "Preparar vaso", PersonalNoteCategory.MEDICATION) }
            advanceUntilIdle()
            assertEquals(1, calls)
            assertEquals(listOf(note), model.state.value.notes)
            assertFalse(model.state.value.composing)
        }
}
