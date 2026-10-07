package com.vitahealth.tata.identity.presentation.subscription

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.identity.application.NoConnection
import com.vitahealth.tata.identity.application.essentialPlan
import com.vitahealth.tata.identity.application.familyPlan
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
}
