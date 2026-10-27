package com.vitahealth.tata.intake.presentation.viewmodel.agenda

import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.queries.GetIntakeAgendaQueryHandler
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/** Verifies that a non-cooperative old load cannot overwrite a later week's result. */
@OptIn(ExperimentalCoroutinesApi::class)
class IntakeAgendaViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun oldWeekFailureCannotReplaceTheNewWeeksResult() {
        lateinit var pending: Continuation<AppResult<List<DoseDetailReadModel>>>
        var calls = 0
        val repository =
            object : IntakeAgendaRepository {
                override suspend fun getAgenda(
                    olderAdultId: String,
                    from: Instant,
                    to: Instant,
                ): AppResult<List<DoseDetailReadModel>> {
                    calls++
                    return if (calls == 1) suspendCoroutine { pending = it }
                    else AppResult.Success(emptyList())
                }
            }
        val model = IntakeAgendaViewModel("adult", GetIntakeAgendaQueryHandler(repository))
        model.refresh()
        val oldWeek = model.state.value.week
        model.moveWeek(1)
        pending.resume(AppResult.Failure("Old week failed", code = "NETWORK_UNAVAILABLE"))
        assertEquals(oldWeek.start.plusWeeks(1), model.state.value.week.start)
        assertEquals(null, model.state.value.error)
        assertFalse(model.state.value.loading)
        assertEquals(2, calls)
    }
}
