package com.vitahealth.tata.carelink.presentation.profiles.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.commands.GenerateCareLinkCodeCommand
import com.vitahealth.tata.carelink.application.commands.GenerateCareLinkCodeCommandHandler
import com.vitahealth.tata.carelink.application.commands.RegisterOlderAdultCommand
import com.vitahealth.tata.carelink.application.commands.RegisterOlderAdultCommandHandler
import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.queries.ListCaregiverProfilesQuery
import com.vitahealth.tata.carelink.application.queries.ListCaregiverProfilesQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads linked adults and registers each profile once before generating or renewing its code. */
class CaregiverProfilesViewModel(
    private val caregiverId: String,
    private val listProfiles: ListCaregiverProfilesQueryHandler,
    private val registerAdult: RegisterOlderAdultCommandHandler,
    private val generateCode: GenerateCareLinkCodeCommandHandler,
) : ViewModel() {
    private val mutable = MutableStateFlow(CaregiverProfilesState())
    val state = mutable.asStateFlow()

    init {
        refresh()
    }

    private var hasResumed = false

    /** Returning from consent or treatment rechecks links without discarding an editable form. */
    fun onResume() {
        if (hasResumed && !state.value.showForm && state.value.code == null) refresh()
        hasResumed = true
    }

    fun refresh() {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = listProfiles(ListCaregiverProfilesQuery(caregiverId))
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    mutable.update { it.copy(adults = result.value, busy = false) }
                is AppResult.Failure -> publishFailure(result.code)
            }
        }
    }

    fun form() {
        if (state.value.busy) return
        mutable.update { it.copy(showForm = true, error = null) }
    }

    fun name(value: String) = editDraft { it.copy(name = value.take(120)) }

    fun date(value: LocalDate) = editDraft { it.copy(birthDate = value) }

    fun contact(value: String) = editDraft { it.copy(contactName = value) }

    fun relationship(value: String) = editDraft { it.copy(relationship = value) }

    fun phone(value: String) = editDraft { it.copy(phone = value) }

    private fun editDraft(change: (CaregiverProfilesState) -> CaregiverProfilesState) {
        mutable.update { current ->
            if (current.busy || current.createdAdult != null) current
            else change(current).copy(error = null)
        }
    }

    fun save() {
        val current = state.value
        if (current.busy) return
        val profile =
            if (current.createdAdult == null) validatedProfile(current) ?: return else null
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val adult =
                current.createdAdult
                    ?: run {
                        val result =
                            registerAdult(
                                RegisterOlderAdultCommand(caregiverId, requireNotNull(profile))
                            )
                        currentCoroutineContext().ensureActive()
                        when (result) {
                            is AppResult.Success -> result.value
                            is AppResult.Failure -> {
                                publishFailure(result.code)
                                return@launch
                            }
                        }
                    }
            currentCoroutineContext().ensureActive()
            mutable.update { it.copy(createdAdult = adult) }
            val result = generateCode(GenerateCareLinkCodeCommand(caregiverId, adult))
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    mutable.update { it.copy(busy = false, code = result.value, showForm = false) }
                is AppResult.Failure -> publishFailure(result.code)
            }
        }
    }

    /** Clears clinical identities and drafts when the initiating account no longer has access. */
    private fun publishFailure(code: String?) {
        val failure = CaregiverProfilesState(error = code)
        mutable.update { current ->
            if (failure.requiresAccountRecovery) failure
            else current.copy(busy = false, error = code)
        }
    }

    private fun validatedProfile(current: CaregiverProfilesState): NewAdultProfile? {
        val date = current.birthDate
        if (current.name.isBlank() || date == null || date.isAfter(LocalDate.now())) {
            mutable.update { it.copy(error = "VALIDATION_ERROR") }
            return null
        }
        val contact = listOf(current.contactName, current.relationship, current.phone)
        if (contact.any { it.isNotBlank() } && contact.any { it.isBlank() }) {
            mutable.update { it.copy(error = "CONTACT_INCOMPLETE") }
            return null
        }
        return NewAdultProfile(
            current.name,
            date,
            current.contactName,
            current.relationship,
            current.phone,
        )
    }

    /** Supplies the three profile CQRS handlers to this account-scoped ViewModel. */
    class Factory(
        private val caregiverId: String,
        private val listProfiles: ListCaregiverProfilesQueryHandler,
        private val registerAdult: RegisterOlderAdultCommandHandler,
        private val generateCode: GenerateCareLinkCodeCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CaregiverProfilesViewModel::class.java))
            return CaregiverProfilesViewModel(
                caregiverId = caregiverId,
                listProfiles = listProfiles,
                registerAdult = registerAdult,
                generateCode = generateCode,
            )
                as T
        }
    }
}
