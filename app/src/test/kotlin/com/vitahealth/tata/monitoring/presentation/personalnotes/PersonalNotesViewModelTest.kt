package com.vitahealth.tata.monitoring.presentation.personalnotes

import com.vitahealth.tata.monitoring.application.commands.CreatePersonalNoteCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetPersonalNotesQueryHandler
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel.PersonalNotesViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalNotesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun oldListCannotEraseTheCreatedNoteAndRefreshIsIgnoredWhileSaving() =
        runTest(dispatcher) {
            val oldList = CompletableDeferred<AppResult<List<PersonalNote>>>()
            val pendingSave = CompletableDeferred<AppResult<PersonalNote>>()
            val note =
                PersonalNote(
                    9,
                    "Agua",
                    "Preparar vaso",
                    PersonalNoteCategory.ROUTINE,
                    Instant.EPOCH,
                )
            var listCalls = 0
            var saveCalls = 0
            val repository =
                object : PersonalNotesRepository {
                    override suspend fun list(): AppResult<List<PersonalNote>> {
                        listCalls++
                        return oldList.await()
                    }

                    override suspend fun create(
                        title: String,
                        text: String,
                        category: PersonalNoteCategory,
                    ): AppResult<PersonalNote> {
                        saveCalls++
                        return pendingSave.await()
                    }
                }
            val model =
                PersonalNotesViewModel(
                    GetPersonalNotesQueryHandler(repository),
                    CreatePersonalNoteCommandHandler(repository),
                )
            advanceUntilIdle()
            model.compose(true)
            model.save(note.title, note.text, note.category)
            advanceUntilIdle()
            model.refresh()
            model.save(note.title, note.text, note.category)
            advanceUntilIdle()

            assertEquals(1, listCalls)
            assertEquals(1, saveCalls)
            assertFalse(model.state.value.loading)
            pendingSave.complete(AppResult.Success(note))
            oldList.complete(AppResult.Success(emptyList()))
            advanceUntilIdle()

            assertEquals(listOf(note), model.state.value.notes)
            assertFalse(model.state.value.saving)
            assertFalse(model.state.value.composing)
        }

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
            val model =
                PersonalNotesViewModel(
                    GetPersonalNotesQueryHandler(repository),
                    CreatePersonalNoteCommandHandler(repository),
                )
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
            val model =
                PersonalNotesViewModel(
                    GetPersonalNotesQueryHandler(repository),
                    CreatePersonalNoteCommandHandler(repository),
                )
            advanceUntilIdle()
            model.compose(true)
            repeat(2) { model.save(" Agua ", "Preparar vaso", PersonalNoteCategory.MEDICATION) }
            advanceUntilIdle()
            assertEquals(1, calls)
            assertEquals(listOf(note), model.state.value.notes)
            assertFalse(model.state.value.composing)
        }

    @Test
    fun accessFailureDuringSaveClearsNotesComposerAndRequiresSignIn() =
        runTest(dispatcher) {
            for (code in
                listOf(
                    "SESSION_REQUIRED",
                    "SESSION_CHANGED",
                    "OLDER_ADULT_SESSION_REQUIRED",
                    "ACCESS_DENIED",
                )) {
                val note =
                    PersonalNote(
                        1,
                        "Title",
                        "Private text",
                        PersonalNoteCategory.ROUTINE,
                        Instant.EPOCH,
                    )
                val repository =
                    object : PersonalNotesRepository {
                        override suspend fun list() = AppResult.Success(listOf(note))

                        override suspend fun create(
                            title: String,
                            text: String,
                            category: PersonalNoteCategory,
                        ): AppResult<PersonalNote> = AppResult.Failure("Access lost", code = code)
                    }
                val model =
                    PersonalNotesViewModel(
                        GetPersonalNotesQueryHandler(repository),
                        CreatePersonalNoteCommandHandler(repository),
                    )
                advanceUntilIdle()
                assertEquals(listOf(note), model.state.value.notes)
                model.compose(true)
                model.save("Title", "Private text", PersonalNoteCategory.ROUTINE)
                advanceUntilIdle()
                assertTrue(model.state.value.notes.isEmpty())
                assertTrue(model.state.value.requiresSignIn)
                assertFalse(model.state.value.composing)
                assertFalse(model.state.value.saving)
                model.compose(true)
                assertFalse(model.state.value.composing)
            }
        }
}
