package com.vitahealth.tata.app.integration.linkedperson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonEmergencyContactCommand
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonEmergencyContactCommandHandler
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonProfileCommand
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonProfileCommandHandler
import com.vitahealth.tata.carelink.application.queries.ListCaregiverProfilesQuery
import com.vitahealth.tata.carelink.application.queries.ListCaregiverProfilesQueryHandler
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.ports.MonitoredPersonReader
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQuery
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQueryHandler
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A read-only application projection composed from the public Monitoring and Treatment ports. */
data class LinkedPersonSnapshot(
    val person: MonitoredPerson,
    val summary: FamilySummary,
    val treatments: List<TreatmentDetailReadModel>,
    val linkedAt: java.time.Instant? = null,
)

fun interface LinkedPersonSnapshotReader {
    suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        displayName: String,
    ): AppResult<LinkedPersonSnapshot>
}

/** Deliberately no direct repository or cross-bounded-context persistence access. */
class LinkedPersonIntegration(
    private val summary: GetFamilySummaryQueryHandler,
    private val person: MonitoredPersonReader,
    private val treatments: ListTreatmentsQueryHandler,
    private val links: ListCaregiverProfilesQueryHandler,
) : LinkedPersonSnapshotReader {
    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        displayName: String,
    ): AppResult<LinkedPersonSnapshot> {
        if (caregiverId.isBlank() || olderAdultId.isBlank()) {
            return AppResult.Failure("Vínculo no identificado.", code = "INVALID_REFERENCE")
        }
        val authorizedLinks =
            when (val result = links(ListCaregiverProfilesQuery(caregiverId))) {
                is AppResult.Failure -> return result
                is AppResult.Success -> result.value
            }
        val link =
            authorizedLinks.singleOrNull { it.id == olderAdultId }
                ?: return AppResult.Failure(
                    "Consentimiento retirado o vínculo inexistente.",
                    code = "CARE_RELATIONSHIP_REQUIRED",
                )
        val profile =
            when (val result = person.read(caregiverId, olderAdultId)) {
                is AppResult.Failure -> return result
                is AppResult.Success -> result.value
            }
        if (profile.id != olderAdultId) {
            return AppResult.Failure("Perfil no coincide.", code = "INVALID_RESPONSE")
        }
        val overview =
            when (
                val result =
                    summary(
                        GetFamilySummaryQuery(
                            caregiverId,
                            olderAdultId,
                            profile.fullName,
                            LocalDate.now(),
                            ZoneId.systemDefault(),
                        )
                    )
            ) {
                is AppResult.Failure -> return result
                is AppResult.Success -> result.value
            }
        val catalog =
            when (val result = treatments(ListTreatmentsQuery(caregiverId, olderAdultId))) {
                is AppResult.Failure -> return result
                is AppResult.Success -> result.value
            }
        if (catalog.any { it.olderAdultId != olderAdultId }) {
            return AppResult.Failure("Tratamiento de otra persona.", code = "INVALID_RESPONSE")
        }
        return AppResult.Success(LinkedPersonSnapshot(profile, overview, catalog, link.confirmedAt))
    }
}

data class LinkedPersonUiState(
    val loading: Boolean = true,
    val snapshot: LinkedPersonSnapshot? = null,
    val error: String? = null,
    val accessDenied: Boolean = false,
    val contactBusy: Boolean = false,
    val contactError: String? = null,
    val pendingContact: ContactChannel? = null,
    val editing: Boolean = false,
    val editName: String = "",
    val editBirthDate: String = "",
    val editSaving: Boolean = false,
    val editError: String? = null,
    val editingContact: Boolean = false,
    val contactName: String = "",
    val contactRelationship: String = "",
    val contactPhone: String = "",
)

class LinkedPersonViewModel(
    private val caregiverId: String,
    private val adultId: String,
    private val displayName: String,
    private val reader: LinkedPersonSnapshotReader,
    private val contactReader: ContactOptionReader,
    private val profileUpdater: UpdateLinkedPersonProfileCommandHandler? = null,
    private val emergencyContactUpdater: UpdateLinkedPersonEmergencyContactCommandHandler? = null,
) : ViewModel() {
    private val mutable = MutableStateFlow(LinkedPersonUiState())
    val state = mutable.asStateFlow()
    private var fetch: Job? = null
    private var contacting: Job? = null
    private var editJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        fetch?.cancel()
        contacting?.cancel()
        editJob?.cancel()
        mutable.value = LinkedPersonUiState(loading = true)
        fetch =
            viewModelScope.launch {
                val result = reader.read(caregiverId, adultId, displayName)
                currentCoroutineContext().ensureActive()
                mutable.value =
                    when (result) {
                        is AppResult.Success ->
                            LinkedPersonUiState(loading = false, snapshot = result.value)
                        is AppResult.Failure ->
                            LinkedPersonUiState(
                                loading = false,
                                error =
                                    if (isAccessFailure(result.code))
                                        "El vínculo o la sesión ya no permite consultar esta ficha."
                                    else "No pudimos cargar la ficha. Comprueba tu conexión.",
                                accessDenied = isAccessFailure(result.code),
                            )
                    }
            }
    }

    fun beginEdit() {
        val person = state.value.snapshot?.person ?: return
        if (state.value.loading || state.value.accessDenied || profileUpdater == null) return
        mutable.update {
            it.copy(
                editing = true,
                editingContact = false,
                editName = person.fullName,
                editBirthDate = person.birthDate?.toString().orEmpty(),
                editError = null,
            )
        }
    }

    fun setEditName(value: String) {
        mutable.update {
            if (!it.editing || it.editSaving) it
            else it.copy(editName = value.take(160), editError = null)
        }
    }

    fun setEditBirthDate(value: String) {
        mutable.update {
            if (!it.editing || it.editSaving) it
            else it.copy(editBirthDate = value.take(10), editError = null)
        }
    }

    fun cancelEdit() {
        if (!state.value.editSaving) mutable.update { it.copy(editing = false, editError = null) }
    }

    fun beginContactEdit() {
        if (!state.value.editing || state.value.editSaving || emergencyContactUpdater == null)
            return
        mutable.update { it.copy(editingContact = true, editError = null) }
    }

    fun setContactField(field: String, value: String) {
        mutable.update {
            if (!it.editing || !it.editingContact || it.editSaving) it
            else
                when (field) {
                    "name" -> it.copy(contactName = value.take(160), editError = null)
                    "relationship" ->
                        it.copy(contactRelationship = value.take(80), editError = null)
                    "phone" -> it.copy(contactPhone = value.take(40), editError = null)
                    else -> it
                }
        }
    }

    private fun saveContact() {
        val current = state.value
        val updater = emergencyContactUpdater ?: return
        if (
            !current.editing ||
                !current.editingContact ||
                current.editSaving ||
                current.loading ||
                current.accessDenied ||
                current.snapshot == null
        )
            return
        mutable.update { it.copy(editSaving = true, editError = null) }
        editJob =
            viewModelScope.launch {
                val result =
                    updater(
                        UpdateLinkedPersonEmergencyContactCommand(
                            caregiverId,
                            adultId,
                            current.contactName,
                            current.contactRelationship,
                            current.contactPhone,
                        )
                    )
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> refresh()
                    is AppResult.Failure ->
                        if (isAccessFailure(result.code)) {
                            denyAccess("El vínculo o la sesión ya no permite modificar esta ficha.")
                        } else
                            mutable.update {
                                it.copy(
                                    editSaving = false,
                                    editError =
                                        if (result.code == "VALIDATION_ERROR")
                                            "Completa nombre, relación y teléfono del contacto."
                                        else "No pudimos guardar el contacto. Inténtalo otra vez.",
                                )
                            }
                }
            }
    }

    fun saveEdit() {
        if (state.value.editingContact) {
            saveContact()
            return
        }
        val current = state.value
        val updater = profileUpdater ?: return
        if (!current.editing || current.editSaving || current.loading || current.snapshot == null)
            return
        val name = current.editName.trim()
        val birth =
            try {
                LocalDate.parse(current.editBirthDate)
            } catch (_: Exception) {
                null
            }
        if (
            name.isBlank() || name.length > 160 || birth == null || birth.isAfter(LocalDate.now())
        ) {
            mutable.update {
                it.copy(editError = "Escribe un nombre y fecha de nacimiento válida (AAAA-MM-DD).")
            }
            return
        }
        if (
            name == current.snapshot.person.fullName && birth == current.snapshot.person.birthDate
        ) {
            cancelEdit()
            return
        }
        mutable.update { it.copy(editSaving = true, editError = null) }
        editJob =
            viewModelScope.launch {
                val result =
                    updater(UpdateLinkedPersonProfileCommand(caregiverId, adultId, name, birth))
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> refresh()
                    is AppResult.Failure ->
                        if (isAccessFailure(result.code)) {
                            denyAccess("El vínculo o la sesión ya no permite modificar esta ficha.")
                        } else
                            mutable.update {
                                it.copy(
                                    editSaving = false,
                                    editError =
                                        "No pudimos guardar los cambios. Inténtalo otra vez.",
                                )
                            }
                }
            }
    }

    fun contact(type: ContactChannelType) {
        if (
            state.value.loading ||
                state.value.snapshot == null ||
                state.value.contactBusy ||
                state.value.accessDenied
        )
            return
        contacting?.cancel()
        mutable.update { it.copy(contactBusy = true, pendingContact = null, contactError = null) }
        contacting =
            viewModelScope.launch {
                val result = contactReader(GetContactOptionQuery(caregiverId, adultId))
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success ->
                        mutable.update {
                            it.copy(
                                contactBusy = false,
                                pendingContact =
                                    result.value.channel?.takeIf { channel ->
                                        channel.type == type
                                    },
                                contactError =
                                    if (result.value.channel?.type != type)
                                        "No hay un canal de contacto de este tipo registrado."
                                    else null,
                            )
                        }
                    is AppResult.Failure -> {
                        if (isAccessFailure(result.code)) {
                            denyAccess("El vínculo o la sesión ya no permite consultar esta ficha.")
                        } else
                            mutable.update {
                                it.copy(
                                    contactBusy = false,
                                    contactError = "No pudimos consultar el contacto.",
                                )
                            }
                    }
                }
            }
    }

    fun contactHandled(launched: Boolean) {
        mutable.update {
            it.copy(
                pendingContact = null,
                contactError =
                    if (launched) null
                    else "No hay una aplicación disponible para abrir este contacto.",
            )
        }
    }

    private fun denyAccess(message: String) {
        fetch?.cancel()
        contacting?.cancel()
        editJob?.cancel()
        mutable.value = LinkedPersonUiState(loading = false, accessDenied = true, error = message)
    }

    private fun isAccessFailure(code: String?) =
        code in
            setOf(
                "SESSION_REQUIRED",
                "UNAUTHENTICATED",
                "AUTHENTICATION_REQUIRED",
                "SESSION_CHANGED",
                "CARE_RELATIONSHIP_REQUIRED",
                "RESOURCE_ACCESS_DENIED",
                "ACCOUNT_ACCESS_DENIED",
            )

    class Factory(
        private val caregiverId: String,
        private val adultId: String,
        private val displayName: String,
        private val reader: LinkedPersonSnapshotReader,
        private val contactReader: ContactOptionReader,
        private val profileUpdater: UpdateLinkedPersonProfileCommandHandler? = null,
        private val emergencyContactUpdater: UpdateLinkedPersonEmergencyContactCommandHandler? =
            null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LinkedPersonViewModel::class.java))
            return LinkedPersonViewModel(
                caregiverId,
                adultId,
                displayName,
                reader,
                contactReader,
                profileUpdater,
                emergencyContactUpdater,
            )
                as T
        }
    }
}
