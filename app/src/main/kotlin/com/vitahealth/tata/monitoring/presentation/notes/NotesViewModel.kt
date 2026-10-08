package com.vitahealth.tata.monitoring.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.handlers.GetFollowUpNotesQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQuery
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.problemOf
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data object Empty : NotesUiState
    data class Content(val notes: List<FollowUpNote>) : NotesUiState
    data class Error(val problem: AlertsProblem) : NotesUiState
}

class NotesViewModel(
    val caregiverId: String,
    private val olderAdultId: String,
    private val getNotes: GetFollowUpNotesQueryHandler,
    registerNote: RegisterFollowUpNoteCommandHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
) : ViewModel() {
    private val mutableState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val state: StateFlow<NotesUiState> = mutableState.asStateFlow()
    val composer = NoteComposer(caregiverId, olderAdultId, registerNote, viewModelScope, context)
    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        val current = mutableState.value
        if (current !is NotesUiState.Content) mutableState.value = NotesUiState.Loading
        job = viewModelScope.launch(context) {
            mutableState.value = when (val result = getNotes(GetFollowUpNotesQuery(caregiverId, olderAdultId))) {
                is AppResult.Success -> if (result.value.isEmpty()) NotesUiState.Empty else NotesUiState.Content(result.value)
                is AppResult.Failure -> NotesUiState.Error(problemOf(result.code))
            }
        }
    }

    /** The saved note goes on top at once; the next refresh brings the server order. */
    fun saveNote(text: String) = composer.save(text) { note ->
        val notes = (mutableState.value as? NotesUiState.Content)?.notes.orEmpty()
        mutableState.value = NotesUiState.Content(listOf(note) + notes.filter { it.id != note.id })
    }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val getNotes: GetFollowUpNotesQueryHandler,
        private val registerNote: RegisterFollowUpNoteCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotesViewModel(caregiverId, olderAdultId, getNotes, registerNote) as T
    }
}
