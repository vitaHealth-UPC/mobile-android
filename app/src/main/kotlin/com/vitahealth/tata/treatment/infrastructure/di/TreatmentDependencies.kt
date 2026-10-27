package com.vitahealth.tata.treatment.infrastructure.di

import androidx.lifecycle.SavedStateHandle
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.commands.CreateTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.commands.DeactivateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.commands.RegisterMedicationCommandHandler
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommandHandler
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommandHandler
import com.vitahealth.tata.treatment.application.commands.SetScheduleInstructionsCommandHandler
import com.vitahealth.tata.treatment.application.commands.UpdateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.ports.TreatmentAdherenceReader
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQueryHandler
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteMedicationManagementRepository
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentCatalogRepository
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentDetailRepository
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentLifecycleRepository
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentRepository
import com.vitahealth.tata.treatment.infrastructure.remote.TreatmentApiService
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementViewModel
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationRegistrationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentCreationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDoseFrequencyViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentListViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentReminderViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentScheduleInstructionsViewModel
import retrofit2.Retrofit

/** Composes medication management and the treatment configuration flow within this context. */
internal class TreatmentDependencies(
    retrofit: Retrofit,
    private val adherenceForScreen: () -> TreatmentAdherenceReader? = { null },
) {
    private val treatmentApi: TreatmentApiService = retrofit.create(TreatmentApiService::class.java)
    private val treatmentRepository = RemoteTreatmentRepository(treatmentApi)
    private val medicationManagementRepository = RemoteMedicationManagementRepository(treatmentApi)
    private val treatmentLifecycleRepository = RemoteTreatmentLifecycleRepository(treatmentApi)
    private val treatmentDetailRepository = RemoteTreatmentDetailRepository(treatmentApi)

    private val treatmentCatalog = RemoteTreatmentCatalogRepository(treatmentApi)

    /** Read-only cross-context projection for a linked-person screen. */
    fun linkedPersonTreatmentsReader() = ListTreatmentsQueryHandler(treatmentCatalog)

    /** Creates the treatment catalog for a linked adult. */
    fun treatmentListViewModelFactory(caregiverId: String, olderAdultId: String) =
        TreatmentListViewModel.Factory(
            caregiverId,
            olderAdultId,
            ListTreatmentsQueryHandler(treatmentCatalog),
        )

    /** Creates medication registration for the selected adult. */
    fun medicationRegistrationViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
    ) =
        MedicationRegistrationViewModel.Factory(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            registerMedicationHandler = RegisterMedicationCommandHandler(treatmentRepository),
        )

    /** Shares the medication adapter across listing, editing and deactivation. */
    fun medicationManagementViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
    ) =
        MedicationManagementViewModel.Factory(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            listMedications = ListMedicationsQueryHandler(medicationManagementRepository),
            updateMedication = UpdateMedicationCommandHandler(medicationManagementRepository),
            deactivateMedication =
                DeactivateMedicationCommandHandler(medicationManagementRepository),
        )

    /** Creates the initial treatment draft for a registered medication. */
    fun treatmentCreationViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
    ) =
        TreatmentCreationViewModel.Factory(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
            createTreatmentHandler = CreateTreatmentCommandHandler(treatmentRepository),
        )

    /** Creates the dose and frequency configuration step. */
    fun treatmentDoseFrequencyViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
        draftState: SavedStateHandle? = null,
    ) =
        TreatmentDoseFrequencyViewModel.Factory(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
            treatmentId = treatmentId,
            treatmentName = treatmentName,
            handler = SetDoseFrequencyCommandHandler(),
            draftState = draftState,
        )

    /** Creates the administration schedule and instructions step. */
    fun treatmentScheduleInstructionsViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
        dosage: String,
        frequency: String,
        draftState: SavedStateHandle? = null,
    ) =
        TreatmentScheduleInstructionsViewModel.Factory(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
            treatmentId = treatmentId,
            treatmentName = treatmentName,
            dosage = dosage,
            frequency = frequency,
            handler = SetScheduleInstructionsCommandHandler(),
            draftState = draftState,
        )

    /** Creates the reminder policy configuration step. */
    fun treatmentReminderViewModelFactory(
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
        draftState: SavedStateHandle? = null,
    ) =
        TreatmentReminderViewModel.Factory(
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
            handler = SetReminderPolicyCommandHandler(),
            draftState = draftState,
        )

    /** Composes regimen persistence and treatment status changes. */
    fun treatmentLifecycleViewModelFactory(
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
        initialStatus: TreatmentStatus = TreatmentStatus.DRAFT,
    ) =
        TreatmentLifecycleViewModel.Factory(
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
            configureTreatmentHandler =
                ConfigureTreatmentCommandHandler(treatmentLifecycleRepository),
            changeStatusHandler = ChangeTreatmentStatusCommandHandler(treatmentLifecycleRepository),
            currentTreatment = GetTreatmentDetailQueryHandler(treatmentDetailRepository),
            medications = ListMedicationsQueryHandler(medicationManagementRepository),
            initialStatus = initialStatus,
            adherence = adherenceForScreen(),
            reactivation = medicationManagementRepository,
        )

    /** Creates the persisted treatment detail flow. */
    fun treatmentDetailViewModelFactory(
        caregiverId: String,
        olderAdultName: String,
        treatmentId: String,
        medicationLabelHint: String,
    ) =
        TreatmentDetailViewModel.Factory(
            caregiverId = caregiverId,
            olderAdultName = olderAdultName,
            treatmentId = treatmentId,
            medicationLabelHint = medicationLabelHint,
            handler = GetTreatmentDetailQueryHandler(treatmentDetailRepository),
        )
}
