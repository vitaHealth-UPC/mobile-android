package com.vitahealth.tata.identity.presentation.subscription

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.identity.application.NoConnection
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommandHandler
import com.vitahealth.tata.identity.application.essentialPlan
import com.vitahealth.tata.identity.application.familyPlan
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.application.queries.ListAvailablePlansQueryHandler
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanChangeMessage
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanMessage
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanSubscriptionViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
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

    private fun viewModel() =
        PlanSubscriptionViewModel(
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

        assertEquals(PlanMessage.ErrorAccount, model.state.value.message)
        assertNull(model.state.value.subscription)
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

    @Test
    fun failedCatalogReloadClearsPriorPlanAndDisablesStaleChange() {
        val backing = FakeSubscriptionRepository()
        var failCatalog = false
        val remote = object : SubscriptionRepository by backing {
            override suspend fun listPlans() =
                if (failCatalog) NoConnection else backing.listPlans()
        }
        val model = PlanSubscriptionViewModel(
            "account-1",
            GetCurrentSubscriptionQueryHandler(remote),
            ListAvailablePlansQueryHandler(remote),
            ChangeSubscriptionCommandHandler(remote),
        )
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        failCatalog = true
        model.load()

        assertEquals(PlanMessage.ErrorOffline, model.state.value.message)
        assertNull(model.state.value.subscription)
        assertTrue(model.state.value.plans.isEmpty())
        assertNull(model.state.value.selectedPlan)
        assertNull(model.state.value.confirming)
        model.onChangeRequest()
        model.onChangeConfirm()
        assertTrue(backing.changes.isEmpty())
    }

    @Test
    fun selectionMustBelongToTheLatestAuthorizedCatalog() {
        val backing = FakeSubscriptionRepository()
        var available = listOf(essentialPlan, familyPlan)
        val remote = object : SubscriptionRepository by backing {
            override suspend fun listPlans() = AppResult.Success(available)
        }
        val model = PlanSubscriptionViewModel(
            "account-1",
            GetCurrentSubscriptionQueryHandler(remote),
            ListAvailablePlansQueryHandler(remote),
            ChangeSubscriptionCommandHandler(remote),
        )
        model.onSelectPlan(essentialPlan.copy(code = "FOREIGN"))
        assertNull(model.state.value.selectedPlan)
        model.onSelectPlan(essentialPlan)
        assertEquals(essentialPlan, model.state.value.selectedPlan)

        available = listOf(familyPlan)
        model.load()
        assertNull(model.state.value.selectedPlan)
        model.onChangeRequest()
        assertNull(model.state.value.confirming)
        assertTrue(backing.changes.isEmpty())
    }

    @Test
    fun confirmingAPlanChangeCancelsAnOlderReload() {
        val delayed = CompletableDeferred<Subscription>()
        var reads = 0
        var reloadCancelled = false
        val backing = FakeSubscriptionRepository()
        val remote =
            object : SubscriptionRepository by backing {
                override suspend fun currentSubscription(
                    accountId: String
                ): AppResult<Subscription> {
                    reads++
                    if (reads == 1) return backing.currentSubscription(accountId)
                    return try {
                        AppResult.Success(delayed.await())
                    } finally {
                        reloadCancelled = true
                    }
                }
            }
        val model =
            PlanSubscriptionViewModel(
                "account-1",
                GetCurrentSubscriptionQueryHandler(remote),
                ListAvailablePlansQueryHandler(remote),
                ChangeSubscriptionCommandHandler(remote),
            )
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        model.load()
        assertTrue(model.state.value.isLoading)

        model.onChangeConfirm()

        assertTrue(reloadCancelled)
        assertFalse(model.state.value.isLoading)
        assertEquals(essentialPlan, model.state.value.subscription?.plan)
        delayed.complete(Subscription("account-1", familyPlan, SubscriptionStatus.ACTIVE, null))
        assertEquals(essentialPlan, model.state.value.subscription?.plan)
    }

    @Test
    fun deniedReloadClearsPreviouslyLoadedPlanAndSelection() {
        listOf("UNAUTHENTICATED", "SESSION_CHANGED", "ACCOUNT_ACCESS_DENIED").forEach { code ->
            repository.failWith = null
            val model = viewModel()
            model.onSelectPlan(essentialPlan)
            model.onChangeRequest()
            repository.failWith = AppResult.Failure("", code = code)
            model.load()
            assertNull(model.state.value.subscription)
            assertTrue(model.state.value.plans.isEmpty())
            assertNull(model.state.value.selectedPlan)
            assertNull(model.state.value.confirming)
            assertEquals(PlanMessage.ErrorSession, model.state.value.message)
        }
    }

    @Test
    fun nonCooperativeReloadCannotOverwriteAConfirmedPlanChange() {
        var pending: Continuation<AppResult<Subscription>>? = null
        var reads = 0
        val backing = FakeSubscriptionRepository()
        val remote =
            object : SubscriptionRepository by backing {
                override suspend fun currentSubscription(
                    accountId: String
                ): AppResult<Subscription> {
                    if (++reads == 1) return backing.currentSubscription(accountId)
                    return suspendCoroutine { pending = it }
                }
            }
        val model =
            PlanSubscriptionViewModel(
                "account-1",
                GetCurrentSubscriptionQueryHandler(remote),
                ListAvailablePlansQueryHandler(remote),
                ChangeSubscriptionCommandHandler(remote),
            )
        model.onSelectPlan(essentialPlan)
        model.onChangeRequest()
        model.load()
        model.onChangeConfirm()
        pending!!.resume(
            AppResult.Success(
                Subscription("account-1", familyPlan, SubscriptionStatus.ACTIVE, null)
            )
        )
        assertEquals(essentialPlan, model.state.value.subscription?.plan)
        assertEquals(PlanChangeMessage.Updated, model.state.value.changeMessage)
    }
}
