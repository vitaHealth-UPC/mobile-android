package com.vitahealth.tata.identity.infrastructure.di

import android.content.Context
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommandHandler
import com.vitahealth.tata.identity.application.commands.CompleteOnboardingCommandHandler
import com.vitahealth.tata.identity.application.commands.RegisterCaregiverCommandHandler
import com.vitahealth.tata.identity.application.commands.RegisterPinCommandHandler
import com.vitahealth.tata.identity.application.commands.RequestNewVerificationCommandHandler
import com.vitahealth.tata.identity.application.commands.RestoreSessionCommandHandler
import com.vitahealth.tata.identity.application.commands.LinkSocialIdentityCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInSocialCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInWithPinCommandHandler
import com.vitahealth.tata.identity.application.commands.SignOutCommandHandler
import com.vitahealth.tata.identity.application.commands.VerifyCaregiverEmailCommandHandler
import com.vitahealth.tata.identity.application.ports.RememberedAdultProfileStore
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.application.queries.GetOnboardingStatusQueryHandler
import com.vitahealth.tata.identity.application.queries.ListAvailablePlansQueryHandler
import com.vitahealth.tata.identity.infrastructure.local.SharedPreferencesOnboardingRepository
import com.vitahealth.tata.identity.infrastructure.local.SharedPreferencesRememberedAdultProfileStore
import com.vitahealth.tata.identity.infrastructure.remote.IdentityApiService
import com.vitahealth.tata.identity.infrastructure.remote.RemoteIdentityRepository
import com.vitahealth.tata.identity.infrastructure.remote.RemoteSessionAccessRepository
import com.vitahealth.tata.identity.infrastructure.remote.RemoteSubscriptionRepository
import com.vitahealth.tata.identity.infrastructure.remote.SessionApiService
import com.vitahealth.tata.identity.infrastructure.remote.SubscriptionApiService
import com.vitahealth.tata.identity.infrastructure.security.SessionScopedSubscriptionRepository
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessViewModel
import com.vitahealth.tata.identity.presentation.landing.LandingPlansViewModel
import com.vitahealth.tata.identity.presentation.registration.viewmodel.CaregiverRegistrationViewModel
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanSubscriptionViewModel
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import retrofit2.Retrofit

/** Composes account, onboarding and subscription flows around the application's shared session. */
internal class IdentityDependencies(
    context: Context,
    retrofit: Retrofit,
    private val sessions: SessionStore,
    private val baseUrl: String,
) {
    /** Keeps the existing remembered-adult storage contract used by PIN navigation. */
    val rememberedAdultProfiles: RememberedAdultProfileStore =
        SharedPreferencesRememberedAdultProfileStore(context)

    private val sessionAccessRepository =
        RemoteSessionAccessRepository(retrofit.create(SessionApiService::class.java), sessions) {
            captured ->
            ApiClientFactory.create(baseUrl) { captured.accessToken }
                .create(SessionApiService::class.java)
        }
    private val identityRepository =
        RemoteIdentityRepository(retrofit.create(IdentityApiService::class.java), sessions)
    private val onboardingRepository = SharedPreferencesOnboardingRepository(context)
    private val getOnboardingStatus = GetOnboardingStatusQueryHandler(onboardingRepository)
    private val completeOnboarding = CompleteOnboardingCommandHandler(onboardingRepository)

    val passwordRecovery =
        com.vitahealth.tata.identity.infrastructure.remote.RemotePasswordRecovery(
            ApiClientFactory.create(baseUrl) { null }
                .create(
                    com.vitahealth.tata.identity.infrastructure.remote.PasswordRecoveryApi::class
                        .java
                )
        )

    val linkSocialIdentity = LinkSocialIdentityCommandHandler(sessionAccessRepository)

    /** Uses the same authentication adapter for access and sign-out. */
    val sessionAccessViewModelFactory =
        SessionAccessViewModel.Factory(
            SignInCommandHandler(sessionAccessRepository),
            SignInWithPinCommandHandler(sessionAccessRepository),
            RegisterPinCommandHandler(sessionAccessRepository),
            RestoreSessionCommandHandler(sessionAccessRepository),
            SignInSocialCommandHandler(sessionAccessRepository),
        )

    /** Shares the registration repository across initial registration and verification retries. */
    val caregiverRegistrationViewModelFactory =
        CaregiverRegistrationViewModel.Factory(
            registerHandler = RegisterCaregiverCommandHandler(identityRepository),
            verifyHandler = VerifyCaregiverEmailCommandHandler(identityRepository),
            requestNewVerificationHandler = RequestNewVerificationCommandHandler(identityRepository),
        )

    /** Clears the shared authenticated session through the access use case. */
    suspend fun signOut() = SignOutCommandHandler(sessionAccessRepository)()

    /** Reads whether the device has already completed onboarding. */
    fun hasSeenOnboarding(): Boolean = getOnboardingStatus()

    /** Persists onboarding completion using the existing device-local keys. */
    fun completeOnboarding() = completeOnboarding.invoke()

    /** Unauthenticated catalog read for the marketing landing; it never writes subscriptions. */
    val landingPlansViewModelFactory =
        LandingPlansViewModel.Factory(
            ListAvailablePlansQueryHandler(
                RemoteSubscriptionRepository(retrofit.create(SubscriptionApiService::class.java))
            )
        )

    /** Creates account-scoped subscription use cases without sharing mutable screen state. */
    fun planSubscriptionViewModelFactory(accountId: String): PlanSubscriptionViewModel.Factory {
        val subscriptionRepository =
            SessionScopedSubscriptionRepository(
                accountId,
                sessions,
                remoteFor = { captured ->
                    val api =
                        ApiClientFactory.create(baseUrl) { captured.accessToken }
                            .create(SubscriptionApiService::class.java)
                    RemoteSubscriptionRepository(api)
                },
            )
        return PlanSubscriptionViewModel.Factory(
            accountId = accountId,
            getSubscription = GetCurrentSubscriptionQueryHandler(subscriptionRepository),
            listPlans = ListAvailablePlansQueryHandler(subscriptionRepository),
            changeSubscription = ChangeSubscriptionCommandHandler(subscriptionRepository),
        )
    }
}
