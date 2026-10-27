package com.vitahealth.tata.treatment

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToString
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.application.commands.DeactivateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.commands.UpdateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.repositories.MedicationManagementRepository
import com.vitahealth.tata.treatment.presentation.medication.MedicationManagementRoute
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Figma #24 -> authorized medication confirmation, never an automatic command. */
@RunWith(AndroidJUnit4::class)
class TreatmentTargetedMedicationDeactivationTest {
    @get:Rule val compose = createComposeRule()

    private class ControlledRepository(
        private val denied: Boolean = false,
    ) : MedicationManagementRepository {
        var deactivations = 0
        private val med = Medication("med-1", "adult-1", "Losartán", "50 mg", true)

        override suspend fun listMedications(
            caregiverId: String,
            olderAdultId: String,
        ): AppResult<List<Medication>> {
            assertEquals("caregiver-1", caregiverId)
            assertEquals("adult-1", olderAdultId)
            return if (denied) {
                AppResult.Failure("No care relationship", code = "CARE_LINK_NOT_AUTHORIZED")
            } else {
                AppResult.Success(listOf(med))
            }
        }

        override suspend fun updateMedication(
            caregiverId: String,
            medicationId: String,
            name: String,
            presentation: String,
        ): AppResult<Medication> = error("No edit requested")

        override suspend fun deactivateMedication(
            caregiverId: String,
            medicationId: String,
        ): AppResult<Medication> {
            assertEquals("caregiver-1", caregiverId)
            assertEquals("med-1", medicationId)
            deactivations++
            return AppResult.Success(med.copy(active = false))
        }
    }

    private fun screen(repo: ControlledRepository, requested: String) {
        // Separate owner for each test; the AndroidTest activity is reused by the runner.
        val testOwner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        compose.setContent {
            val base = LocalContext.current
            val spanish = remember(base) { base.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
                LocalViewModelStoreOwner provides testOwner,
            ) {
                TataTheme {
                    MedicationManagementRoute(
                        factory = MedicationManagementViewModel.Factory(
                            "caregiver-1",
                            "adult-1",
                            "Rosa Vargas",
                            ListMedicationsQueryHandler(repo),
                            UpdateMedicationCommandHandler(repo),
                            DeactivateMedicationCommandHandler(repo),
                        ),
                        onBack = {},
                        onAddMedication = {},
                        initialDeactivationMedicationId = requested,
                    )
                }
            }
        }
    }

    private val confirmationTitles = listOf("¿Desactivar Losartán?", "Deactivate Losartán?")
    private val cancelLabels = listOf("Cancelar", "Cancel")
    private val confirmLabels = listOf("Desactivar", "Deactivate")

    private fun visibleLabel(candidates: List<String>): String? =
        candidates.firstOrNull {
            compose.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
        }

    private fun waitForConfirmation(): Pair<String, String> {
        // Compose Dialog creates a separate window whose locale can follow the
        // test activity rather than the composition's LocalContext override.
        try {
            compose.waitUntil(15000) { visibleLabel(cancelLabels) != null }
        } catch (e: Throwable) {
            throw AssertionError(
                "Confirmation absent. Current Compose tree:\n" +
                    compose.onAllNodes(isRoot(), useUnmergedTree = true).printToString(maxDepth = 12),
                e,
            )
        }
        val title = requireNotNull(visibleLabel(confirmationTitles)) {
            "A dialog opened, but not for the authorized Losartán medication"
        }
        return title to requireNotNull(visibleLabel(cancelLabels))
    }

    @Test
    fun opensExactMedicationConfirmationWithoutAutomaticWrite() {
        val repository = ControlledRepository()
        screen(repository, "med-1")
        val (title, cancel) = waitForConfirmation()
        compose.onNodeWithText(title).assertExists()
        compose.runOnIdle { assertEquals(0, repository.deactivations) }
        compose.onNodeWithText(cancel).performClick()
        confirmationTitles.forEach { compose.onNodeWithText(it).assertDoesNotExist() }
        compose.runOnIdle { assertEquals(0, repository.deactivations) }
    }

    @Test
    fun revokedCareLinkAndWrongMedicationNeverOpenDeactivation() {
        val repository = ControlledRepository(denied = true)
        screen(repository, "med-1")
        compose.waitUntil(15000) {
            compose.onAllNodesWithText("Acceso restringido").fetchSemanticsNodes().isNotEmpty()
        }
        confirmationTitles.forEach { compose.onNodeWithText(it).assertDoesNotExist() }
        compose.runOnIdle { assertEquals(0, repository.deactivations) }
    }

    @Test
    fun confirmingExactMedicationDispatchesOneWrite() {
        val repository = ControlledRepository()
        screen(repository, "med-1")
        val (title, _) = waitForConfirmation()
        val confirm = requireNotNull(visibleLabel(confirmLabels)) {
            "The exact medication confirmation has no confirm button"
        }
        compose.runOnIdle { assertEquals(0, repository.deactivations) }
        compose.onNodeWithText(confirm).performClick()
        compose.waitUntil(15000) { repository.deactivations == 1 }
        compose.onNodeWithText(title).assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, repository.deactivations) }
    }

    @Test
    fun unknownTargetCannotPromptForAnotherMedication() {
        val repository = ControlledRepository()
        screen(repository, "another-medication")
        compose.waitUntil(15000) {
            compose.onAllNodesWithText("Losartán").fetchSemanticsNodes().isNotEmpty()
        }
        confirmationTitles.forEach { compose.onNodeWithText(it).assertDoesNotExist() }
        compose.runOnIdle { assertEquals(0, repository.deactivations) }
    }
}
