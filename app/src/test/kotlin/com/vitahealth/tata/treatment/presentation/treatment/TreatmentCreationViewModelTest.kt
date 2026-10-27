package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentCreationViewModel
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.CreateTreatmentCommandHandler
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.TreatmentRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TreatmentCreationViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun savingRetainsSubmittedNameAndCancelledResponseCannotAdvanceWizard() {
        lateinit var pending: Continuation<AppResult<Treatment>>
        var calls = 0
        val repository =
            Proxy.newProxyInstance(
                TreatmentRepository::class.java.classLoader,
                arrayOf(TreatmentRepository::class.java),
            ) { _, _, args ->
                calls++
                assertEquals("Presión", args[2])
                @Suppress("UNCHECKED_CAST")
                pending = args.last() as Continuation<AppResult<Treatment>>
                COROUTINE_SUSPENDED
            } as TreatmentRepository
        val model =
            TreatmentCreationViewModel(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartán",
                CreateTreatmentCommandHandler(repository),
            )
        model.onNameChange("Presión")
        model.createTreatment()
        model.onNameChange("Otro nombre")
        model.createTreatment()
        assertEquals("Presión", model.state.value.name)
        assertEquals(1, calls)
        model.viewModelScope.cancel()
        pending.resume(
            AppResult.Success(Treatment("treatment", "adult", "Presión", TreatmentStatus.DRAFT))
        )
        assertNull(model.state.value.createdTreatment)
    }
}
