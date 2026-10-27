package com.vitahealth.tata.treatment

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.application.commands.*
import com.vitahealth.tata.treatment.application.ports.*
import com.vitahealth.tata.treatment.application.queries.*
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.*
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleRoute
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleViewModel
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TreatmentMedicationReactivationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cancelDoesNotWriteAndConfirmationRefreshesWithoutResuming() {
        var active = false
        var writes = 0
        val mutations =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, _, _ ->
                error("Reactivation must not resume treatment")
            } as TreatmentLifecycleRepository
        val catalog =
            Proxy.newProxyInstance(
                MedicationManagementRepository::class.java.classLoader,
                arrayOf(MedicationManagementRepository::class.java),
            ) { _, method, _ ->
                check(method.name == "listMedications")
                AppResult.Success(
                    listOf(Medication("medication", "adult", "Losartan", "50 mg", active))
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
                            TreatmentStatus.PAUSED,
                            "medication",
                            "1 tableta",
                            "Diaria",
                            listOf("08:00"),
                            "Con agua",
                            7,
                            10,
                        )
                    )
            }
        compose.setContent {
            TataTheme {
                TreatmentLifecycleRoute(
                    TreatmentLifecycleViewModel.Factory(
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
                        ConfigureTreatmentCommandHandler(mutations),
                        ChangeTreatmentStatusCommandHandler(mutations),
                        currentTreatment = GetTreatmentDetailQueryHandler(detail),
                        medications = ListMedicationsQueryHandler(catalog),
                        initialStatus = TreatmentStatus.PAUSED,
                        reactivation =
                            MedicationReactivationPort { caregiver, medication ->
                                assertEquals("caregiver", caregiver)
                                assertEquals("medication", medication)
                                writes++
                                active = true
                                AppResult.Success(
                                    Medication("medication", "adult", "Losartan", "50 mg", true)
                                )
                            },
                    )
                )
            }
        }
        compose.onNodeWithText("Reactivar").performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(0, writes) }
        compose.onNodeWithText("Inactivo").assertExists()
        compose.onNodeWithText("Reactivar").performScrollTo().performClick()
        compose.onNode(isDialog()).onChild().assertExists()
        compose
            .onAllNodesWithText("Reactivar")
            .filter(hasAnyAncestor(isDialog()))
            .onFirst()
            .performClick()
        compose.onNodeWithText("Reanudar").assertIsEnabled()
        compose.onNodeWithText("Pausado").assertExists()
        compose.onNodeWithText("1 medicamento activo").assertExists()
        compose.onNodeWithText("Editar pauta").assertIsEnabled()
        compose.runOnIdle { assertEquals(1, writes) }
    }
}
