package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommand
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommand
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.commands.TreatmentLifecycleAction
import com.vitahealth.tata.treatment.application.ports.MedicationReactivationPort
import com.vitahealth.tata.treatment.application.ports.TreatmentAdherenceReader
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQuery
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQuery
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Coordinates regimen configuration and treatment activation, pause and resume. */
class TreatmentLifecycleViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    medicationId: String,
    medicationLabel: String,
    treatmentId: String,
    treatmentName: String,
    dosage: String,
    frequency: String,
    scheduleText: String,
    instructions: String,
    reminderDelayMinutes: Int,
    private val configureTreatmentHandler: ConfigureTreatmentCommandHandler,
    private val changeStatusHandler: ChangeTreatmentStatusCommandHandler,
    private val currentTreatment: GetTreatmentDetailQueryHandler? = null,
    private val medications: ListMedicationsQueryHandler? = null,
    private val initialStatus: TreatmentStatus = TreatmentStatus.DRAFT,
    private val adherence: TreatmentAdherenceReader? = null,
    private val reactivation: MedicationReactivationPort? = null,
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            TreatmentLifecycleUiState(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                dosage = dosage,
                frequency = frequency,
                scheduleText = scheduleText,
                instructions = instructions,
                reminderDelayMinutes = reminderDelayMinutes,
                status = initialStatus,
            )
        )
    val state: StateFlow<TreatmentLifecycleUiState> = _state.asStateFlow()

    private var hasResumed = false

    init {
        if (initialStatus != TreatmentStatus.DRAFT) refreshManagement()
        if (medications != null && initialStatus == TreatmentStatus.DRAFT)
            viewModelScope.launch {
                val result = medications(ListMedicationsQuery(caregiverId, olderAdultId))
                currentCoroutineContext().ensureActive()
                if (
                    result is AppResult.Success &&
                        result.value.all { it.olderAdultId == olderAdultId } &&
                        !_state.value.managementReadFailed
                ) {
                    _state.update {
                        it.copy(
                            activeMedicationCount =
                                result.value.count { medication -> medication.active },
                            medicationLabel =
                                result.value
                                    .firstOrNull { medication -> medication.id == it.medicationId }
                                    ?.let { medication ->
                                        listOf(medication.name, medication.presentation)
                                            .filter(String::isNotBlank)
                                            .joinToString(" · ")
                                    } ?: it.medicationLabel,
                        )
                    }
                }
            }
    }

    /** Revalidates restored management data without replacing an unsubmitted wizard. */
    fun onResume() {
        if (hasResumed) refreshManagement()
        hasResumed = true
    }

    fun refreshManagement() {
        val reader = currentTreatment ?: return
        val requested = _state.value
        if (requested.status == TreatmentStatus.DRAFT || requested.isLoading) return
        _state.update {
            it.copy(
                isLoading = true,
                isRefreshingManagement = true,
                errorMessage = null,
                adherence = null,
                adherenceError = null,
                regimenSaved = false,
            )
        }
        viewModelScope.launch {
            val result =
                reader(GetTreatmentDetailQuery(requested.caregiverId, requested.treatmentId))
            currentCoroutineContext().ensureActive()
            val detail = (result as? AppResult.Success)?.value
            if (
                detail == null ||
                    detail.olderAdultId != requested.olderAdultId ||
                    detail.medicationId != requested.medicationId ||
                    detail.dose == null ||
                    detail.frequency == null ||
                    detail.scheduledTimes.isEmpty() ||
                    detail.status == TreatmentStatus.DRAFT
            ) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshingManagement = false,
                        managementReadFailed = true,
                        activeMedicationCount = null,
                        errorMessage =
                            if (result is AppResult.Failure) lifecycleMessage(result)
                            else "La pauta cambió. Vuelve al detalle y ábrela nuevamente.",
                    )
                }
                return@launch
            }
            val catalog =
                medications?.invoke(
                    ListMedicationsQuery(requested.caregiverId, requested.olderAdultId)
                )
            currentCoroutineContext().ensureActive()
            val entries = (catalog as? AppResult.Success)?.value
            val medication = entries?.firstOrNull { it.id == requested.medicationId }
            if (
                catalog is AppResult.Failure ||
                    (entries != null &&
                        (entries.any { it.olderAdultId != requested.olderAdultId } ||
                            medication == null))
            ) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshingManagement = false,
                        managementReadFailed = true,
                        activeMedicationCount = null,
                        errorMessage =
                            if (catalog is AppResult.Failure) lifecycleMessage(catalog)
                            else "No se pudo verificar el medicamento de esta pauta.",
                    )
                }
                return@launch
            }
            val metrics = adherence?.read(requested.olderAdultId, requested.treatmentId)
            currentCoroutineContext().ensureActive()
            if (metrics is AppResult.Failure && invalidatesManagement(metrics)) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshingManagement = false,
                        managementReadFailed = true,
                        activeMedicationCount = null,
                        adherence = null,
                        errorMessage = lifecycleMessage(metrics),
                    )
                }
                return@launch
            }
            _state.update {
                it.copy(
                    status = detail.status,
                    medicationInactive = medication?.active == false,
                    activeMedicationCount =
                        entries?.count { it.active } ?: it.activeMedicationCount,
                    medicationLabel =
                        medication?.let { value -> "${value.name} · ${value.presentation}" }
                            ?: it.medicationLabel,
                    treatmentName = detail.name,
                    dosage = detail.dose,
                    frequency = detail.frequency,
                    scheduleText = detail.scheduledTimes.joinToString(","),
                    instructions = detail.instructions,
                    reminderDelayMinutes = detail.followUpDelayMinutes ?: 0,
                    usesDefaultFollowUp = detail.followUpDelayMinutes == null,
                    isLoading = false,
                    isRefreshingManagement = false,
                    managementReadFailed = false,
                    errorMessage = null,
                    adherence = (metrics as? AppResult.Success)?.value,
                    adherenceError =
                        (metrics as? AppResult.Failure)?.let { failure ->
                            lifecycleMessage(failure)
                        },
                )
            }
        }
    }

    fun activate() {
        val current = _state.value
        if (
            current.isLoading ||
                current.managementReadFailed ||
                current.status != TreatmentStatus.DRAFT
        )
            return
        _state.update { it.copy(isLoading = true, errorMessage = null, regimenSaved = false) }
        viewModelScope.launch {
            // Recheck the server before mutating a restored or stale wizard.
            val reader = currentTreatment
            if (reader != null) {
                when (
                    val latest =
                        reader(GetTreatmentDetailQuery(current.caregiverId, current.treatmentId))
                ) {
                    is AppResult.Failure -> {
                        currentCoroutineContext().ensureActive()
                        _state.update {
                            it.copy(isLoading = false, errorMessage = lifecycleMessage(latest))
                        }
                        return@launch
                    }
                    is AppResult.Success -> {
                        currentCoroutineContext().ensureActive()
                        if (latest.value.olderAdultId != current.olderAdultId) {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "El tratamiento no corresponde a esta persona.",
                                )
                            }
                            return@launch
                        }
                        if (latest.value.status != TreatmentStatus.DRAFT) {
                            _state.update {
                                it.copy(
                                    status = latest.value.status,
                                    isLoading = false,
                                    errorMessage = null,
                                )
                            }
                            return@launch
                        }
                    }
                }
            }
            val configured =
                configureTreatmentHandler(
                    ConfigureTreatmentCommand(
                        caregiverId = current.caregiverId,
                        treatmentId = current.treatmentId,
                        medicationId = current.medicationId,
                        dose = current.dosage,
                        frequency = current.frequency,
                        scheduledTimes = current.scheduleText.split(","),
                        instructions = current.instructions,
                        reminderLeadMinutes = 0,
                        followUpDelayMinutes = current.reminderDelayMinutes,
                    )
                )
            currentCoroutineContext().ensureActive()
            if (configured is AppResult.Failure) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = lifecycleMessage(configured),
                        configurationIncomplete =
                            configured.code in
                                setOf(
                                    "INVALID_TREATMENT_CONFIGURATION",
                                    "REQUEST_VALIDATION_FAILED",
                                ),
                    )
                }
                return@launch
            }

            applyStatus(TreatmentLifecycleAction.ACTIVATE)
        }
    }

    /** Updates only the existing regimen; editing never changes ACTIVE/PAUSED lifecycle state. */
    fun editRegimen(
        dose: String,
        frequency: String,
        schedule: String,
        instructions: String,
        delay: String,
    ) {
        val current = _state.value
        if (
            current.isLoading ||
                current.managementReadFailed ||
                current.medicationInactive ||
                current.status == TreatmentStatus.DRAFT
        )
            return
        val minutes = delay.trim().toIntOrNull()
        if ((delay.isNotBlank() && minutes == null) || (minutes != null && minutes !in 0..1440)) {
            _state.update {
                it.copy(errorMessage = "Usa un plazo entre 0 y 1440 minutos.", regimenSaved = false)
            }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, regimenSaved = false) }
        viewModelScope.launch {
            val latest =
                currentTreatment?.invoke(
                    GetTreatmentDetailQuery(current.caregiverId, current.treatmentId)
                )
            currentCoroutineContext().ensureActive()
            if (
                latest !is AppResult.Success ||
                    latest.value.olderAdultId != current.olderAdultId ||
                    latest.value.medicationId != current.medicationId
            ) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        managementReadFailed =
                            (latest is AppResult.Failure && invalidatesManagement(latest)) ||
                                (latest is AppResult.Success),
                        errorMessage =
                            if (latest is AppResult.Failure) lifecycleMessage(latest)
                            else "No se pudo verificar la pauta actual.",
                    )
                }
                return@launch
            }
            val leadMinutes = latest.value.reminderLeadMinutes ?: 0
            val result =
                configureTreatmentHandler(
                    ConfigureTreatmentCommand(
                        current.caregiverId,
                        current.treatmentId,
                        current.medicationId,
                        dose,
                        frequency,
                        schedule.split(","),
                        instructions,
                        leadMinutes,
                        minutes,
                    )
                )
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(
                            dosage = dose.trim(),
                            frequency = frequency.trim(),
                            scheduleText = schedule.trim(),
                            instructions = instructions.trim(),
                            reminderDelayMinutes = minutes ?: 0,
                            usesDefaultFollowUp = minutes == null,
                            regimenSaved = true,
                            status = result.value.status,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                is AppResult.Failure ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = lifecycleMessage(result),
                            managementReadFailed =
                                it.managementReadFailed ||
                                    (it.status != TreatmentStatus.DRAFT &&
                                        invalidatesManagement(result)),
                        )
                    }
            }
        }
    }

    /** An explicit availability change never dispatches a treatment resume. */
    fun reactivateMedication() {
        val current = _state.value
        val operation = reactivation ?: return
        if (current.isLoading || current.managementReadFailed || !current.medicationInactive) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = operation.reactivate(current.caregiverId, current.medicationId)
            currentCoroutineContext().ensureActive()
            if (
                result is AppResult.Success &&
                    result.value.id == current.medicationId &&
                    result.value.olderAdultId == current.olderAdultId &&
                    result.value.active
            ) {
                _state.update { it.copy(isLoading = false) }
                refreshManagement()
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        managementReadFailed =
                            result is AppResult.Failure && invalidatesManagement(result),
                        errorMessage =
                            if (result is AppResult.Failure) lifecycleMessage(result)
                            else "No se pudo verificar la reactivación.",
                    )
                }
            }
        }
    }

    fun pause() {
        if (_state.value.status == TreatmentStatus.ACTIVE)
            changeStatus(TreatmentLifecycleAction.PAUSE)
    }

    fun resume() {
        if (_state.value.status == TreatmentStatus.PAUSED)
            changeStatus(TreatmentLifecycleAction.RESUME)
    }

    private fun changeStatus(action: TreatmentLifecycleAction) {
        if (
            _state.value.isLoading ||
                _state.value.managementReadFailed ||
                _state.value.medicationInactive
        )
            return
        _state.update { it.copy(isLoading = true, errorMessage = null, regimenSaved = false) }
        viewModelScope.launch { applyStatus(action) }
    }

    private suspend fun applyStatus(action: TreatmentLifecycleAction) {
        val current = _state.value
        val result =
            changeStatusHandler(
                ChangeTreatmentStatusCommand(
                    caregiverId = current.caregiverId,
                    treatmentId = current.treatmentId,
                    action = action,
                )
            )
        currentCoroutineContext().ensureActive()
        when (result) {
            is AppResult.Success ->
                _state.update {
                    it.copy(status = result.value.status, isLoading = false, errorMessage = null)
                }
            is AppResult.Failure ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = lifecycleMessage(result),
                        managementReadFailed =
                            it.managementReadFailed ||
                                (it.status != TreatmentStatus.DRAFT &&
                                    invalidatesManagement(result)),
                    )
                }
        }
    }

    private fun invalidatesManagement(failure: AppResult.Failure): Boolean =
        failure.code in
            setOf(
                "CARE_LINK_NOT_AUTHORIZED",
                "SESSION_REQUIRED",
                "ACCESS_DENIED",
                "SESSION_CHANGED",
                "RESOURCE_OWNER_MISMATCH",
                "RESOURCE_NOT_FOUND",
            )

    private fun lifecycleMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "CARE_LINK_NOT_AUTHORIZED" -> "El vínculo de cuidado ya no está activo."
            "RESOURCE_NOT_FOUND" -> "No encontramos el tratamiento solicitado."
            "CONFLICT" -> "El tratamiento no puede cambiar a ese estado."
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            "INVALID_TREATMENT_CONFIGURATION" ->
                "Completa una pauta válida antes de activar el tratamiento."
            else -> failure.message
        }

    /** Supplies the reviewed regimen and lifecycle use cases for a compatible model type. */
    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val medicationId: String,
        private val medicationLabel: String,
        private val treatmentId: String,
        private val treatmentName: String,
        private val dosage: String,
        private val frequency: String,
        private val scheduleText: String,
        private val instructions: String,
        private val reminderDelayMinutes: Int,
        private val configureTreatmentHandler: ConfigureTreatmentCommandHandler,
        private val changeStatusHandler: ChangeTreatmentStatusCommandHandler,
        private val currentTreatment: GetTreatmentDetailQueryHandler? = null,
        private val medications: ListMedicationsQueryHandler? = null,
        private val initialStatus: TreatmentStatus = TreatmentStatus.DRAFT,
        private val adherence: TreatmentAdherenceReader? = null,
        private val reactivation: MedicationReactivationPort? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TreatmentLifecycleViewModel::class.java)) {
                "Unsupported ViewModel: ${modelClass.name}"
            }
            return TreatmentLifecycleViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                dosage = dosage,
                frequency = frequency,
                scheduleText = scheduleText,
                instructions = instructions,
                reminderDelayMinutes = reminderDelayMinutes,
                configureTreatmentHandler = configureTreatmentHandler,
                changeStatusHandler = changeStatusHandler,
                currentTreatment = currentTreatment,
                medications = medications,
                initialStatus = initialStatus,
                adherence = adherence,
                reactivation = reactivation,
            )
                as T
        }
    }
}
