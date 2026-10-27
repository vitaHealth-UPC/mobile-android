package com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.commands.CreatePersonalNoteCommand
import com.vitahealth.tata.monitoring.application.commands.CreatePersonalNoteCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetPersonalNotesQuery
import com.vitahealth.tata.monitoring.application.queries.GetPersonalNotesQueryHandler
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Serializes note creation against list reloads while retaining the active composer on failure. */
class PersonalNotesViewModel(
    private val listNotes: GetPersonalNotesQueryHandler,
    private val createNote: CreatePersonalNoteCommandHandler,
) : ViewModel() {
    private val mutable = MutableStateFlow(PersonalNotesUiState())
    val state = mutable.asStateFlow()
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun filter(category: PersonalNoteCategory?) {
        mutable.update { it.copy(category = category) }
    }

    fun compose(open: Boolean) {
        if (!mutable.value.saving && !mutable.value.requiresSignIn)
            mutable.update { it.copy(composing = open, saveError = false) }
    }

    fun refresh() {
        if (mutable.value.saving) return
        loadJob?.cancel()
        mutable.update { it.copy(loading = true, error = null) }
        loadJob =
            viewModelScope.launch {
                when (
                    val result =
                        listNotes(GetPersonalNotesQuery).also {
                            currentCoroutineContext().ensureActive()
                        }
                ) {
                    is AppResult.Success ->
                        mutable.update { it.copy(notes = result.value, loading = false) }
                    is AppResult.Failure ->
                        mutable.update {
                            val failed = it.copy(loading = false, error = result.code)
                            if (failed.requiresSignIn)
                                PersonalNotesUiState(loading = false, error = result.code)
                            else failed
                        }
                }
            }
    }

    fun save(title: String, text: String, category: PersonalNoteCategory) {
        if (
            mutable.value.requiresSignIn ||
                mutable.value.saving ||
                title.isBlank() ||
                text.isBlank() ||
                title.length > 100 ||
                text.length > 1000
        )
            return
        loadJob?.cancel()
        mutable.update { it.copy(saving = true, loading = false, saveError = false) }
        viewModelScope.launch {
            when (
                val result =
                    createNote(CreatePersonalNoteCommand(title, text, category)).also {
                        currentCoroutineContext().ensureActive()
                    }
            ) {
                is AppResult.Success ->
                    mutable.update {
                        it.copy(
                            notes =
                                listOf(result.value) +
                                    it.notes.filter { note -> note.id != result.value.id },
                            saving = false,
                            composing = false,
                        )
                    }
                is AppResult.Failure ->
                    mutable.update {
                        val failed = it.copy(error = result.code)
                        if (failed.requiresSignIn)
                            PersonalNotesUiState(loading = false, error = result.code)
                        else it.copy(saving = false, saveError = true)
                    }
            }
        }
    }

    class Factory(
        private val listNotes: GetPersonalNotesQueryHandler,
        private val createNote: CreatePersonalNoteCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PersonalNotesViewModel(listNotes, createNote) as T
    }
}
