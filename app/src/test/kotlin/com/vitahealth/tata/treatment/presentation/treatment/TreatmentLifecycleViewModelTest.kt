package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.ports.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.MedicationManagementRepository
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleViewModel
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TreatmentLifecycleViewModelTest {
    @Test
    fun returningAfterDeactivationRefreshesCatalogAndBlocksAllRegimenMutations() {
        var active = true
        var reads = 0
        val lifecycle =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, _, _ ->
                error("Inactive medication must not mutate")
            } as TreatmentLifecycleRepository
        val catalog =
            Proxy.newProxyInstance(
                MedicationManagementRepository::class.java.classLoader,
                arrayOf(MedicationManagementRepository::class.java),
            ) { _, method, _ ->
                check(method.name == "listMedications")
                reads++
                AppResult.Success(
                    listOf(
                        Medication(
                            "medication",
                            "adult",
                            if (active) "Anterior" else "Actualizado",
                            "50 mg",
                            active,
                        )
                    )
                )
            } as MedicationManagementRepository
        val detail =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(caregiverId: String, treatmentId: String) =
                    AppResult.Success(
                        TreatmentDetailReadModel(
                            "treatment",
                            "adult",
                            "Control",
                            if (active) TreatmentStatus.ACTIVE else TreatmentStatus.PAUSED,
                            "medication",
                            "1 tableta",
                            "Diaria",
                            listOf("08:00"),
                            "",
                            7,
                            null,
                        )
                    )
            }
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartan",
                "treatment",
                "Control",
                "1 tableta",
                "Diaria",
                "08:00",
                "",
                0,
                ConfigureTreatmentCommandHandler(lifecycle),
                ChangeTreatmentStatusCommandHandler(lifecycle),
                currentTreatment = GetTreatmentDetailQueryHandler(detail),
                medications = ListMedicationsQueryHandler(catalog),
                initialStatus = TreatmentStatus.ACTIVE,
            )
        assertEquals(1, reads)
        assertEquals(1, model.state.value.activeMedicationCount)
        assertEquals(false, model.state.value.medicationInactive)
        model.onResume()
        active = false
        model.onResume()
        assertEquals(2, reads)
        assertEquals(0, model.state.value.activeMedicationCount)
        assertEquals(true, model.state.value.medicationInactive)
        assertEquals("Actualizado · 50 mg", model.state.value.medicationLabel)
        assertEquals(TreatmentStatus.PAUSED, model.state.value.status)
        model.resume()
        model.pause()
        model.editRegimen("2 tabletas", "Diaria", "09:00", "", "10")
        assertEquals(false, model.state.value.isLoading)
        model.viewModelScope.cancel()
    }

    @Test
    fun managementAdherenceClearsOnFailureAndRevocationPreventsWrites() {
        var result:
            AppResult<
                com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel?
            > =
            AppResult.Success(
                com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel(
                    75,
                    emptyList(),
                )
            )
        val lifecycle =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, _, _ ->
                error("No mutation expected")
            } as TreatmentLifecycleRepository
        val reader =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(caregiverId: String, treatmentId: String) =
                    AppResult.Success(
                        TreatmentDetailReadModel(
                            "treatment",
                            "adult",
                            "Control",
                            TreatmentStatus.ACTIVE,
                            "medication",
                            "1 tableta",
                            "Diaria",
                            listOf("08:00"),
                            "",
                            7,
                            null,
                        )
                    )
            }
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartan",
                "treatment",
                "Control",
                "1 tableta",
                "Diaria",
                "08:00",
                "",
                0,
                ConfigureTreatmentCommandHandler(lifecycle),
                ChangeTreatmentStatusCommandHandler(lifecycle),
                currentTreatment = GetTreatmentDetailQueryHandler(reader),
                initialStatus = TreatmentStatus.ACTIVE,
                adherence =
                    com.vitahealth.tata.treatment.application.ports.TreatmentAdherenceReader {
                        adult,
                        treatment ->
                        assertEquals("adult", adult)
                        assertEquals("treatment", treatment)
                        result
                    },
            )
        assertEquals(75, model.state.value.adherence?.percent)
        result = AppResult.Failure("Sin conexion", code = "NETWORK_UNAVAILABLE")
        model.refreshManagement()
        assertEquals(null, model.state.value.adherence)
        assertEquals(false, model.state.value.managementReadFailed)
        assertEquals(true, model.state.value.adherenceError != null)
        result = AppResult.Success(null)
        model.refreshManagement()
        assertEquals(null, model.state.value.adherenceError)
        result = AppResult.Failure("Revocado", code = "ACCESS_DENIED")
        model.refreshManagement()
        assertEquals(true, model.state.value.managementReadFailed)
        model.pause()
        model.editRegimen("2 tabletas", "Diaria", "08:00", "", "10")
        model.viewModelScope.cancel()
    }

    @Test
    fun factoryRejectsAnUnrelatedModelType() {
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, _, _ ->
                error("Factory must not perform a request")
            } as TreatmentLifecycleRepository
        val factory =
            TreatmentLifecycleViewModel.Factory(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartán",
                "treatment",
                "Presión",
                "1 comprimido",
                "Diaria",
                "08:00",
                "Con agua",
                10,
                ConfigureTreatmentCommandHandler(repository),
                ChangeTreatmentStatusCommandHandler(repository),
            )
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnrelatedViewModel::class.java)
        }
        assertEquals(
            TreatmentStatus.DRAFT,
            factory.create(TreatmentLifecycleViewModel::class.java).state.value.status,
        )
    }

    private class UnrelatedViewModel : ViewModel()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun managingAnExistingStatusDoesNotConfigureOrReactivateIt() {
        for (initial in listOf(TreatmentStatus.ACTIVE, TreatmentStatus.PAUSED)) {
            val calls = mutableListOf<String>()
            val next =
                if (initial == TreatmentStatus.ACTIVE) TreatmentStatus.PAUSED
                else TreatmentStatus.ACTIVE
            val repository =
                Proxy.newProxyInstance(
                    TreatmentLifecycleRepository::class.java.classLoader,
                    arrayOf(TreatmentLifecycleRepository::class.java),
                ) { _, method, args ->
                    calls += method.name
                    assertEquals("caregiver", args[0])
                    assertEquals("treatment", args[1])
                    AppResult.Success(Treatment("treatment", "adult", "Control", next))
                } as TreatmentLifecycleRepository
            val model =
                TreatmentLifecycleViewModel(
                    "caregiver",
                    "adult",
                    "Rosa",
                    "medication",
                    "Losartan",
                    "treatment",
                    "Control",
                    "1 tableta",
                    "Diaria",
                    "08:00",
                    "",
                    10,
                    ConfigureTreatmentCommandHandler(repository),
                    ChangeTreatmentStatusCommandHandler(repository),
                    initialStatus = initial,
                )
            assertEquals(initial, model.state.value.status)
            model.activate()
            assertEquals(emptyList<String>(), calls)
            if (initial == TreatmentStatus.ACTIVE) model.pause() else model.resume()
            assertEquals(
                listOf(
                    if (initial == TreatmentStatus.ACTIVE) "pauseTreatment" else "resumeTreatment"
                ),
                calls,
            )
            assertEquals(next, model.state.value.status)
        }
    }

    @Test
    fun managementRefreshCorrectsStaleStatusAndDenialBlocksAllMutationsUntilRetry() {
        var denied = true
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, _ ->
                calls += method.name
                AppResult.Success(
                    Treatment("treatment", "adult", "Control", TreatmentStatus.ACTIVE)
                )
            } as TreatmentLifecycleRepository
        val reader =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(
                    caregiverId: String,
                    treatmentId: String,
                ): AppResult<TreatmentDetailReadModel> =
                    if (denied) AppResult.Failure("Revoked", code = "CARE_LINK_NOT_AUTHORIZED")
                    else
                        AppResult.Success(
                            TreatmentDetailReadModel(
                                "treatment",
                                "adult",
                                "Control",
                                TreatmentStatus.PAUSED,
                                "medication",
                                "2 tabletas",
                                "Diaria",
                                listOf("09:00"),
                                "Con agua",
                                7,
                                null,
                            )
                        )
            }
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartan",
                "treatment",
                "Control",
                "1 tableta",
                "Diaria",
                "08:00",
                "",
                0,
                ConfigureTreatmentCommandHandler(repository),
                ChangeTreatmentStatusCommandHandler(repository),
                currentTreatment = GetTreatmentDetailQueryHandler(reader),
                initialStatus = TreatmentStatus.ACTIVE,
            )
        assertEquals(true, model.state.value.managementReadFailed)
        model.pause()
        model.resume()
        model.activate()
        model.editRegimen("2 tabletas", "Diaria", "09:00", "", "10")
        assertEquals(emptyList<String>(), calls)
        denied = false
        model.refreshManagement()
        assertEquals(false, model.state.value.managementReadFailed)
        assertEquals(TreatmentStatus.PAUSED, model.state.value.status)
        assertEquals("2 tabletas", model.state.value.dosage)
        assertEquals("09:00", model.state.value.scheduleText)
        assertEquals(true, model.state.value.usesDefaultFollowUp)
        model.pause()
        assertEquals(emptyList<String>(), calls)
        model.resume()
        assertEquals(listOf("resumeTreatment"), calls)
    }

    @Test
    fun editingARegimenWithSystemFollowUpPreservesNullInsteadOfInventingZero() {
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, args ->
                calls += method.name
                assertEquals("configureTreatment", method.name)
                assertEquals(7, args[7])
                assertEquals(null, args[8])
                AppResult.Success(
                    Treatment("treatment", "adult", "Control", TreatmentStatus.PAUSED)
                )
            } as TreatmentLifecycleRepository
        val reader =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(caregiverId: String, treatmentId: String) =
                    AppResult.Success(
                        TreatmentDetailReadModel(
                            "treatment",
                            "adult",
                            "Control",
                            TreatmentStatus.PAUSED,
                            "medication",
                            "1 tableta",
                            "Diaria",
                            listOf("08:00"),
                            "",
                            7,
                            null,
                        )
                    )
            }
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartan",
                "treatment",
                "Control",
                "1 tableta",
                "Diaria",
                "08:00",
                "",
                0,
                ConfigureTreatmentCommandHandler(repository),
                ChangeTreatmentStatusCommandHandler(repository),
                currentTreatment = GetTreatmentDetailQueryHandler(reader),
                initialStatus = TreatmentStatus.PAUSED,
            )
        model.editRegimen("2 tabletas", "Diaria", "09:00", "Con agua", "")
        assertEquals(listOf("configureTreatment"), calls)
        assertEquals(true, model.state.value.usesDefaultFollowUp)
        assertEquals(TreatmentStatus.PAUSED, model.state.value.status)
    }

    @Test
    fun deletedTreatmentDuringPauseOrEditInvalidatesThePreviousManagementRead() {
        for (operation in listOf("pause", "edit")) {
            val calls = mutableListOf<String>()
            val repository =
                Proxy.newProxyInstance(
                    TreatmentLifecycleRepository::class.java.classLoader,
                    arrayOf(TreatmentLifecycleRepository::class.java),
                ) { _, method, _ ->
                    calls += method.name
                    AppResult.Failure("Deleted", code = "RESOURCE_NOT_FOUND")
                } as TreatmentLifecycleRepository
            val reader =
                object : TreatmentDetailRepository {
                    override suspend fun getTreatmentDetail(
                        caregiverId: String,
                        treatmentId: String,
                    ) =
                        AppResult.Success(
                            TreatmentDetailReadModel(
                                "treatment",
                                "adult",
                                "Control",
                                TreatmentStatus.ACTIVE,
                                "medication",
                                "1 tableta",
                                "Diaria",
                                listOf("08:00"),
                                "",
                                0,
                                10,
                            )
                        )
                }
            val model =
                TreatmentLifecycleViewModel(
                    "caregiver",
                    "adult",
                    "Rosa",
                    "medication",
                    "Losartan",
                    "treatment",
                    "Control",
                    "1 tableta",
                    "Diaria",
                    "08:00",
                    "",
                    10,
                    ConfigureTreatmentCommandHandler(repository),
                    ChangeTreatmentStatusCommandHandler(repository),
                    currentTreatment = GetTreatmentDetailQueryHandler(reader),
                    initialStatus = TreatmentStatus.ACTIVE,
                )
            if (operation == "pause") model.pause()
            else model.editRegimen("2 tabletas", "Diaria", "09:00", "", "10")
            assertEquals(true, model.state.value.managementReadFailed)
            assertEquals(false, model.state.value.isLoading)
            model.pause()
            model.editRegimen("2 tabletas", "Diaria", "09:00", "", "10")
            assertEquals(
                listOf(if (operation == "pause") "pauseTreatment" else "configureTreatment"),
                calls,
            )
        }
    }

    @Test
    fun cancelledConfigurationCannotContinueToActivation() {
        lateinit var pending: Continuation<AppResult<Treatment>>
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, args ->
                calls += method.name
                @Suppress("UNCHECKED_CAST")
                pending = args.last() as Continuation<AppResult<Treatment>>
                COROUTINE_SUSPENDED
            } as TreatmentLifecycleRepository
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartán",
                "treatment",
                "Presión",
                "1 comprimido",
                "Diaria",
                "08:00",
                "Con agua",
                10,
                ConfigureTreatmentCommandHandler(repository),
                ChangeTreatmentStatusCommandHandler(repository),
            )
        model.activate()
        model.viewModelScope.cancel()
        pending.resume(
            AppResult.Success(Treatment("treatment", "adult", "Presión", TreatmentStatus.DRAFT))
        )
        assertEquals(listOf("configureTreatment"), calls)
        assertEquals(TreatmentStatus.DRAFT, model.state.value.status)
    }

    @Test
    fun lifecycleActionsOnlySubmitTransitionsAvailableInTheCurrentState() {
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, args ->
                if (method.name == "configureTreatment") {
                    assertEquals(0, args[7])
                    assertEquals(10, args[8])
                }
                calls += method.name
                val status =
                    when (method.name) {
                        "configureTreatment" -> TreatmentStatus.DRAFT
                        "pauseTreatment" -> TreatmentStatus.PAUSED
                        else -> TreatmentStatus.ACTIVE
                    }
                AppResult.Success(Treatment("treatment", "adult", "Presión", status))
            } as TreatmentLifecycleRepository
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartán",
                "treatment",
                "Presión",
                "1 comprimido",
                "Diaria",
                "08:00",
                "Con agua",
                10,
                ConfigureTreatmentCommandHandler(repository),
                ChangeTreatmentStatusCommandHandler(repository),
            )

        model.pause()
        model.resume()
        assertEquals(emptyList<String>(), calls)
        model.activate()
        model.activate()
        model.resume()
        model.pause()
        model.pause()
        model.resume()

        assertEquals(
            listOf("configureTreatment", "activateTreatment", "pauseTreatment", "resumeTreatment"),
            calls,
        )
        assertEquals(TreatmentStatus.ACTIVE, model.state.value.status)
    }

    @Test
    fun restoredWizardNeverReconfiguresAnAlreadyActiveTreatmentOrWritesAfterFailedRead() {
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, _ ->
                calls += method.name
                error("A stale wizard must not write")
            } as TreatmentLifecycleRepository
        for (denied in listOf(false, true)) {
            val reader =
                object : TreatmentDetailRepository {
                    override suspend fun getTreatmentDetail(
                        caregiverId: String,
                        treatmentId: String,
                    ): AppResult<TreatmentDetailReadModel> {
                        assertEquals("caregiver", caregiverId)
                        assertEquals("treatment", treatmentId)
                        return if (denied)
                            AppResult.Failure("Revoked", code = "CARE_LINK_NOT_AUTHORIZED")
                        else
                            AppResult.Success(
                                TreatmentDetailReadModel(
                                    "treatment",
                                    "adult",
                                    "Control",
                                    TreatmentStatus.ACTIVE,
                                    "medication",
                                    "1 tableta",
                                    "DAILY",
                                    listOf("08:00"),
                                    "Con agua",
                                    0,
                                    10,
                                )
                            )
                    }
                }
            val model =
                TreatmentLifecycleViewModel(
                    "caregiver",
                    "adult",
                    "Rosa",
                    "medication",
                    "Losartan",
                    "treatment",
                    "Control",
                    "1 tableta",
                    "Diaria",
                    "08:00",
                    "Con agua",
                    10,
                    ConfigureTreatmentCommandHandler(repository),
                    ChangeTreatmentStatusCommandHandler(repository),
                    GetTreatmentDetailQueryHandler(reader),
                )
            model.activate()
            assertEquals(
                if (denied) TreatmentStatus.DRAFT else TreatmentStatus.ACTIVE,
                model.state.value.status,
            )
            assertEquals(false, model.state.value.isLoading)
            assertEquals(emptyList<String>(), calls)
        }
    }

    @Test
    fun editingPausedRegimenPreservesServerLeadAndDoesNotResumeTreatment() {
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, args ->
                calls += method.name
                assertEquals("configureTreatment", method.name)
                assertEquals(7, args[7])
                assertEquals(25, args[8])
                AppResult.Success(
                    Treatment("treatment", "adult", "Control", TreatmentStatus.PAUSED)
                )
            } as TreatmentLifecycleRepository
        val reader =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(caregiverId: String, treatmentId: String) =
                    AppResult.Success(
                        TreatmentDetailReadModel(
                            "treatment",
                            "adult",
                            "Control",
                            TreatmentStatus.PAUSED,
                            "medication",
                            "1 tableta",
                            "DAILY",
                            listOf("08:00"),
                            "",
                            7,
                            10,
                        )
                    )
            }
        val model =
            TreatmentLifecycleViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartan",
                "treatment",
                "Control",
                "1 tableta",
                "Diaria",
                "08:00",
                "",
                10,
                ConfigureTreatmentCommandHandler(repository),
                ChangeTreatmentStatusCommandHandler(repository),
                GetTreatmentDetailQueryHandler(reader),
            )
        model.activate() // Recovers the server PAUSED status without writing.
        model.editRegimen("2 tabletas", "Diaria", "09:00", "Con agua", "25")
        assertEquals(listOf("configureTreatment"), calls)
        assertEquals(TreatmentStatus.PAUSED, model.state.value.status)
        assertEquals("2 tabletas", model.state.value.dosage)
        assertEquals(25, model.state.value.reminderDelayMinutes)
        assertEquals(true, model.state.value.regimenSaved)
        model.editRegimen("2 tabletas", "Diaria", "09:00", "Con agua", "invalid")
        assertEquals(false, model.state.value.regimenSaved)
        assertEquals(listOf("configureTreatment"), calls)
    }
}
