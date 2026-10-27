package com.vitahealth.tata.monitoring.presentation.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.ports.MonitoredPersonReader
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQuery
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.problemOf
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotesUiState {
    data object Loading : NotesUiState

    data object Empty : NotesUiState

    data class Content(val notes: List<FollowUpNote>) : NotesUiState

    data class Error(val problem: AlertsProblem) : NotesUiState
}

/** Loads caregiver follow-up notes and incorporates acknowledged composer saves. */
class NotesViewModel(
    val caregiverId: String,
    private val olderAdultId: String,
    private val getNotes: GetFollowUpNotesQueryHandler,
    registerNote: RegisterFollowUpNoteCommandHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val personReader: MonitoredPersonReader? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val state: StateFlow<NotesUiState> = mutableState.asStateFlow()
    private val mutablePerson = MutableStateFlow<MonitoredPerson?>(null)
    val person: StateFlow<MonitoredPerson?> = mutablePerson.asStateFlow()
    val composer =
        NoteComposer(
            caregiverId,
            olderAdultId,
            registerNote,
            viewModelScope,
            context,
            onAccessDenied = { denyAccess() },
        )
    private var job: Job? = null

    fun refresh() {
        if (composer.state.value.saving) return
        job?.cancel()
        mutablePerson.value = null
        val current = mutableState.value
        if (current !is NotesUiState.Content) mutableState.value = NotesUiState.Loading
        job =
            viewModelScope.launch(context) {
                mutableState.value =
                    when (
                        val result =
                            getNotes(GetFollowUpNotesQuery(caregiverId, olderAdultId)).also {
                                currentCoroutineContext().ensureActive()
                            }
                    ) {
                        is AppResult.Success ->
                            if (result.value.isEmpty()) NotesUiState.Empty
                            else NotesUiState.Content(result.value)
                        is AppResult.Failure -> {
                            val problem = problemOf(result.code)
                            if (problem == AlertsProblem.ACCESS_DENIED) denyAccess()
                            NotesUiState.Error(problem)
                        }
                    }
                if (
                    mutableState.value is NotesUiState.Content ||
                        mutableState.value == NotesUiState.Empty
                ) {
                    val profile = personReader?.read(caregiverId, olderAdultId)
                    currentCoroutineContext().ensureActive()
                    when (profile) {
                        is AppResult.Success -> mutablePerson.value = profile.value
                        is AppResult.Failure ->
                            if (problemOf(profile.code) == AlertsProblem.ACCESS_DENIED) denyAccess()
                        null -> Unit
                    }
                }
            }
    }

    private fun denyAccess() {
        job?.cancel()
        mutablePerson.value = null
        composer.clear()
        mutableState.value = NotesUiState.Error(AlertsProblem.ACCESS_DENIED)
    }

    /** The saved note goes on top at once; the next refresh brings the server order. */
    fun saveNote(text: String) =
        composer.save(text) { note ->
            job?.cancel()
            val notes = (mutableState.value as? NotesUiState.Content)?.notes.orEmpty()
            mutableState.value =
                NotesUiState.Content(listOf(note) + notes.filter { it.id != note.id })
        }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val getNotes: GetFollowUpNotesQueryHandler,
        private val registerNote: RegisterFollowUpNoteCommandHandler,
        private val personReader: MonitoredPersonReader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotesViewModel(
                caregiverId,
                olderAdultId,
                getNotes,
                registerNote,
                personReader = personReader,
            )
                as T
    }
}
