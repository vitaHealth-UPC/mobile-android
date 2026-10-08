package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.application.handlers.Conflict
import com.vitahealth.tata.treatment.application.handlers.DeactivateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.FakeMedicationManagementRepository
import com.vitahealth.tata.treatment.application.handlers.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.handlers.Missing
import com.vitahealth.tata.treatment.application.handlers.NoConnection
import com.vitahealth.tata.treatment.application.handlers.NotLinked
import com.vitahealth.tata.treatment.application.handlers.UpdateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.losartan
import com.vitahealth.tata.treatment.application.handlers.metformin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationManagementViewModelTest {
    private val repository = FakeMedicationManagementRepository(listOf(losartan, metformin))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = MedicationManagementViewModel(
        caregiverId = "caregiver-1",
        olderAdultId = "adult-1",
        olderAdultName = "Rosa Vargas",
        listMedications = ListMedicationsQueryHandler(repository),
        updateMedication = UpdateMedicationCommandHandler(repository),
        deactivateMedication = DeactivateMedicationCommandHandler(repository),
    )

    @Test
    fun loadsTheMedicationsWhenItOpens() {
        val model = viewModel()

        assertFalse(model.state.value.isLoading)
        assertEquals(listOf(losartan, metformin), model.state.value.medications)
        assertFalse(model.state.value.accessDenied)
    }

    @Test
    fun withoutACareLinkTheAccessIsRestricted() {
        repository.failWith = NotLinked

        val model = viewModel()

        assertTrue(model.state.value.accessDenied)
        assertFalse(model.state.value.loadFailed)
    }

    @Test
    fun aFailedLoadCanBeRetried() {
        repository.failWith = NoConnection
        val model = viewModel()
        assertTrue(model.state.value.loadFailed)
        assertEquals(MedicationManagementMessage.ErrorOffline, model.state.value.message)

        repository.failWith = null
        model.load()

        assertFalse(model.state.value.loadFailed)
        assertEquals(2, model.state.value.medications.size)
    }

    @Test
    fun editingStartsFromTheCurrentValues() {
        val model = viewModel()

        model.onEdit(losartan)

        assertEquals(MedicationDraft("med-1", "Losartán", "50 mg, comprimido"), model.state.value.draft)
    }

    @Test
    fun anInactiveMedicationCannotBeEdited() {
        val model = viewModel()

        model.onEdit(metformin)

        assertNull(model.state.value.draft)
    }

    @Test
    fun savingAnEditReplacesTheMedicationAndConfirms() {
        val model = viewModel()
        model.onEdit(losartan)
        model.onDraftNameChange("Losartán potásico")
        model.onDraftPresentationChange("100 mg, comprimido")

        model.onSaveEdit()

        val saved = model.state.value.medications.first { it.id == "med-1" }
        assertEquals("Losartán potásico", saved.name)
        assertEquals("100 mg, comprimido", saved.presentation)
        assertNull(model.state.value.draft)
        assertEquals(MedicationManagementMessage.Updated, model.state.value.message)
        assertFalse(model.state.value.isSaving)
    }

    @Test
    fun savingWithABlankNameKeepsTheDraftOpenWithoutCallingTheBackend() {
        val model = viewModel()
        model.onEdit(losartan)
        model.onDraftNameChange(" ")

        model.onSaveEdit()

        assertNotNull(model.state.value.draft)
        assertEquals(MedicationManagementMessage.ErrorRequiredFields, model.state.value.message)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun aConflictOnSaveSaysTheMedicationIsInactive() {
        val model = viewModel()
        model.onEdit(losartan)
        repository.failWith = Conflict

        model.onSaveEdit()

        assertEquals(MedicationManagementMessage.ErrorInactive, model.state.value.message)
        assertNotNull(model.state.value.draft)
    }

    @Test
    fun cancellingTheEditDropsTheDraft() {
        val model = viewModel()
        model.onEdit(losartan)

        model.onCancelEdit()

        assertNull(model.state.value.draft)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun deactivatingAsksForConfirmationFirst() {
        val model = viewModel()

        model.onDeactivateRequest(losartan)

        assertEquals(losartan, model.state.value.deactivating)
        assertTrue(repository.deactivations.isEmpty())
    }

    @Test
    fun cancellingTheConfirmationChangesNothing() {
        val model = viewModel()
        model.onDeactivateRequest(losartan)

        model.onDeactivateCancel()

        assertNull(model.state.value.deactivating)
        assertTrue(repository.deactivations.isEmpty())
        assertTrue(model.state.value.medications.first { it.id == "med-1" }.active)
    }

    @Test
    fun confirmingTheDeactivationKeepsTheMedicationInTheListAsInactive() {
        val model = viewModel()
        model.onDeactivateRequest(losartan)

        model.onDeactivateConfirm()

        assertEquals(2, model.state.value.medications.size)
        assertFalse(model.state.value.medications.first { it.id == "med-1" }.active)
        assertEquals(MedicationManagementMessage.Deactivated, model.state.value.message)
        assertNull(model.state.value.deactivating)
    }

    @Test
    fun anAlreadyInactiveMedicationCannotBeDeactivatedAgain() {
        val model = viewModel()

        model.onDeactivateRequest(metformin)

        assertNull(model.state.value.deactivating)
    }

    @Test
    fun aDeactivationThatFailsLeavesTheMedicationActive() {
        val model = viewModel()
        model.onDeactivateRequest(losartan)
        repository.failWith = Missing

        model.onDeactivateConfirm()

        assertTrue(model.state.value.medications.first { it.id == "med-1" }.active)
        assertEquals(MedicationManagementMessage.ErrorNotFound, model.state.value.message)
        assertTrue(model.state.value.messageIsError)
    }

    @Test
    fun deactivatingTheMedicationBeingEditedClosesItsDraft() {
        val model = viewModel()
        model.onEdit(losartan)
        model.onDeactivateRequest(losartan)

        model.onDeactivateConfirm()

        assertNull(model.state.value.draft)
    }
}
