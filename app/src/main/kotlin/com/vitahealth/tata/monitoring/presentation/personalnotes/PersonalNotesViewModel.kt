package com.vitahealth.tata.monitoring.presentation.personalnotes

import androidx.lifecycle.*
import com.vitahealth.tata.monitoring.application.PersonalNotesRepository
import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PersonalNotesUiState(
    val notes: List<PersonalNote> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val saveError: Boolean = false,
    val composing: Boolean = false,
    val category: PersonalNoteCategory? = null,
)

class PersonalNotesViewModel(private val repository: PersonalNotesRepository) : ViewModel() {
    private val mutable = MutableStateFlow(PersonalNotesUiState())
    val state = mutable.asStateFlow()

    init {
        refresh()
    }

    fun filter(category: PersonalNoteCategory?) {
        mutable.update { it.copy(category = category) }
    }

    fun compose(open: Boolean) {
        if (!mutable.value.saving) mutable.update { it.copy(composing = open, saveError = false) }
    }

    fun refresh() {
        viewModelScope.launch {
            mutable.update { it.copy(loading = true, error = null) }
            when (val result = repository.list()) {
                is AppResult.Success ->
                    mutable.update { it.copy(notes = result.value, loading = false) }
                is AppResult.Failure ->
                    mutable.update { it.copy(loading = false, error = result.code) }
            }
        }
    }

    fun save(title: String, text: String, category: PersonalNoteCategory) {
        if (
            mutable.value.saving ||
                title.isBlank() ||
                text.isBlank() ||
                title.length > 100 ||
                text.length > 1000
        )
            return
        mutable.update { it.copy(saving = true, saveError = false) }
        viewModelScope.launch {
            when (val result = repository.create(title.trim(), text.trim(), category)) {
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
                is AppResult.Failure -> mutable.update { it.copy(saving = false, saveError = true) }
            }
        }
    }

    class Factory(private val repository: PersonalNotesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PersonalNotesViewModel(repository) as T
    }
}
