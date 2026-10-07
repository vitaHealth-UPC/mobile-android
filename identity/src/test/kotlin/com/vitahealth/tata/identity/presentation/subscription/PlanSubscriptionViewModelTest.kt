package com.vitahealth.tata.identity.presentation.subscription

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.identity.application.NoConnection
import com.vitahealth.tata.identity.application.essentialPlan
import com.vitahealth.tata.identity.application.familyPlan
import com.vitahealth.tata.identity.application.handlers.ChangeSubscriptionCommandHandler
import com.vitahealth.tata.identity.application.handlers.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.application.handlers.ListAvailablePlansQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
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
class PlanSubscriptionViewModelTest {
    private val repository = FakeSubscriptionRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PlanSubscriptionViewModel(
        accountId = "account-1",
        getSubscription = GetCurrentSubscriptionQueryHandler(repository),
        listPlans = ListAvailablePlansQueryHandler(repository),
        changeSubscription = ChangeSubscriptionCommandHandler(repository),
    )

    @Test
    fun showsTheCurrentPlanAndEveryAvailablePlanWhenItOpens() {
        val model = viewModel()

        val state = model.state.value
        assertFalse(state.isLoading)
        assertEquals(familyPlan, state.subscription?.plan)
        assertEquals(listOf(essentialPlan, familyPlan), state.plans)
        assertNull(state.message)
    }

    @Test
    fun theSubscriptionOfTheOpenedAccountIsRequested() {
        viewModel()

        assertEquals(listOf("account-1"), repository.subscriptionCalls)
    }

    @Test
    fun withoutConnectionItExplainsItAndOffersToRetry() {
        repository.failWith = NoConnection

        val model = viewModel()

        assertNull(model.state.value.subscription)
        assertEquals(PlanMessage.ErrorOffline, model.state.value.message)
        assertFalse(model.state.value.isLoading)
    }

    @Test
    fun retryingAfterAFailureLoadsThePlan() {
        repository.failWith = NoConnection
        val model = viewModel()

        repository.failWith = null
        model.load()

        assertNotNull(model.state.value.subscription)
        assertNull(model.state.value.message)
    }

    @Test
    fun anUnknownAccountIsReportedAsAnAccountProblem() {
        repository.failWith = AppResult.Failure(message = "x", code = "ACCOUNT_NOT_FOUND")

        assertEquals(PlanMessage.ErrorAccount, viewModel().state.value.message)
    }

    @Test
    fun anExpiredSessionIsReportedAsASessionProblem() {
        repository.failWith = AppResult.Failure(message = "x", code = "UNAUTHENTICATED")

        assertEquals(PlanMessage.ErrorSession, viewModel().state.value.message)
    }

    @Test
    fun anyOtherFailureIsGeneric() {
        repository.failWith = AppResult.Failure(message = "x", code = "REQUEST_FAILED")

        assertEquals(PlanMessage.ErrorGeneric, viewModel().state.value.message)
    }

    @Test
    fun theCurrentPlanIsMarkedAmongTheAvailableOnes() {
        repository.subscribedTo = essentialPlan

        val state = viewModel().state.value

        assertEquals(true, state.subscription?.isOn(essentialPlan))
        assertEquals(false, state.subscription?.isOn(familyPlan))
    }

    @Test
    fun theCurrentPlanCannotBeSelected() {
        val model = viewModel()

        model.onSelectPlan(familyPlan)

        assertNull(model.state.value.selectedPlan)
    }

    @Test
    fun tappingAnotherPlanSelectsItAndTappingAgainClearsIt() {
        val model = viewModel()

        model.onSelectPlan(essentialPlan)
        assertEquals(essentialPlan, model.state.value.selectedPlan)

        model.onSelectPlan(essentialPlan)
        assertNull(model.state.value.selectedPlan)
    }

    @Test
    fun changingWithoutASelectionDoesNothing() {
        val model = viewModel()

        model.onChangeRequest()

        assertNull(model.state.value.confirming)
        assertTrue(repository.changes.isEmpty())
    }

    @Test
    fun requestingTheChangeAsksForConfirmationBeforeCallingTheBackend() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)

        model.onChangeRequest()

        assertEquals(essentialPlan, model.state.value.confirming)
        assertTrue(repository.changes.isEmpty())
    }

    @Test
    fun cancellingTheConfirmationKeepsThePlan() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()

        model.onChangeCancel()

        assertNull(model.state.value.confirming)
        assertEquals(familyPlan, model.state.value.subscription?.plan)
        assertTrue(repository.changes.isEmpty())
    }

    @Test
    fun confirmingChangesThePlanAndShowsTheConfirmation() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()

        model.onChangeConfirm()

        val state = model.state.value
        assertEquals(essentialPlan, state.subscription?.plan)
        assertEquals(PlanChangeMessage.Updated, state.changeMessage)
        assertFalse(state.changeMessageIsError)
        assertNull(state.selectedPlan)
        assertNull(state.confirming)
        assertFalse(state.isChanging)
        assertEquals(listOf("account-1" to "ESSENTIAL"), repository.changes)
    }

    @Test
    fun withoutConnectionThePlanStaysAndTheErrorIsShown() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        repository.failWith = NoConnection

        model.onChangeConfirm()

        assertEquals(familyPlan, model.state.value.subscription?.plan)
        assertEquals(PlanChangeMessage.ErrorOffline, model.state.value.changeMessage)
        assertTrue(model.state.value.changeMessageIsError)
        assertEquals(essentialPlan, model.state.value.selectedPlan)
    }

    @Test
    fun aPlanThatIsNoLongerAvailableIsExplained() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        repository.failWith = AppResult.Failure(message = "x", code = "PLAN_NOT_FOUND")

        model.onChangeConfirm()

        assertEquals(PlanChangeMessage.ErrorPlanUnavailable, model.state.value.changeMessage)
    }

    @Test
    fun anInactiveAccountIsReportedAsAnAccountProblem() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        repository.failWith = AppResult.Failure(message = "x", code = "ACCOUNT_NOT_ACTIVE")

        model.onChangeConfirm()

        assertEquals(PlanChangeMessage.ErrorAccount, model.state.value.changeMessage)
    }

    @Test
    fun selectingAPlanClearsThePreviousResult() {
        val model = viewModel()
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        model.onChangeConfirm()
        assertEquals(PlanChangeMessage.Updated, model.state.value.changeMessage)

        model.onSelectPlan(familyPlan)

        assertNull(model.state.value.changeMessage)
    }
}
