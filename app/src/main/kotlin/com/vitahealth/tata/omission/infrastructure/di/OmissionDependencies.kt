package com.vitahealth.tata.omission.infrastructure.di

import android.content.Context
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommand
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommandHandler
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommand
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommandHandler
import com.vitahealth.tata.omission.application.policies.OmissionPushAccessPolicy
import com.vitahealth.tata.omission.application.queries.ResolvePushDestinationQueryHandler
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.application.services.LegacyPushTopicRetirement
import com.vitahealth.tata.omission.application.services.PushDeviceRegistrationCoordinator
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.infrastructure.local.SharedPreferencesPushInstallationStore
import com.vitahealth.tata.omission.infrastructure.local.SharedPreferencesPushTargetStore
import com.vitahealth.tata.omission.infrastructure.push.FirebaseDevicePushTokenSource
import com.vitahealth.tata.omission.infrastructure.push.FirebasePushTopicRemover
import com.vitahealth.tata.omission.infrastructure.remote.PushDeviceApiService
import com.vitahealth.tata.omission.infrastructure.remote.RemoteDeviceTokenRegistry
import com.vitahealth.tata.omission.infrastructure.security.OmissionNotificationSessionCleanup
import com.vitahealth.tata.omission.presentation.notifications.mappers.OmissionPushIntent
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.CoroutineScope

/**
 * Binds the Omission application ports to FCM, HTTP and local Android adapters.
 *
 * The application shell only invokes this facade; it never constructs push repositories or applies
 * recipient policy on behalf of this bounded context.
 */
internal class OmissionDependencies(
    context: Context,
    private val sessions: SessionStore,
    private val observer: SessionObserver,
    baseUrl: String,
    private val pushRegistrationEnabled: Boolean,
) {
    private val appContext = context.applicationContext
    private val targets = SharedPreferencesPushTargetStore(appContext)
    private val accessPolicy = OmissionPushAccessPolicy(sessions, targets)
    private val retiredTopics =
        LegacyPushTopicRetirement(targets, FirebasePushTopicRemover(appContext))
    private val followCaregiverAlerts = FollowCaregiverAlertsCommandHandler(targets)
    private val followDoseReminders = FollowDoseRemindersCommandHandler(targets)
    private val resolveDestination = ResolvePushDestinationQueryHandler(targets)

    private val registration =
        PushDeviceRegistrationCoordinator(
            sessions,
            observer,
            FirebaseDevicePushTokenSource(appContext),
            RemoteDeviceTokenRegistry(
                sessions,
                SharedPreferencesPushInstallationStore(appContext).getOrCreate(),
                { captured ->
                    ApiClientFactory.create(baseUrl) { captured.accessToken }
                        .create(PushDeviceApiService::class.java)
                },
            ),
        )

    private var sessionCleanup: OmissionNotificationSessionCleanup? = null

    fun canDisplay(push: OmissionPush): Boolean = accessPolicy.allows(push)

    fun followCaregiver(caregiverId: String, olderAdultId: String) =
        followCaregiverAlerts(FollowCaregiverAlertsCommand(caregiverId, olderAdultId))

    fun followAdultReminders(olderAdultId: String, olderAdultName: String) =
        followDoseReminders(FollowDoseRemindersCommand(olderAdultId, olderAdultName))

    /** Parse Android extras through Omission's typed boundary and recheck session access. */
    fun destination(
        kind: String?,
        olderAdultId: String?,
        medicationName: String?,
        intakeId: String?,
    ): PushDestination? =
        OmissionPushIntent.queryFrom(kind, olderAdultId, medicationName, intakeId)
            ?.let(resolveDestination::invoke)
            ?.takeIf(accessPolicy::allowsDestination)

    fun canOpen(destination: PushDestination): Boolean = accessPolicy.allowsDestination(destination)

    fun startRegistration(scope: CoroutineScope) {
        retiredTopics.start(scope)
        if (pushRegistrationEnabled) registration.start(scope)
    }

    suspend fun beforeSignOut() = registration.beforeSignOut()

    fun onNewToken(token: String) {
        if (pushRegistrationEnabled) registration.onNewToken(token)
    }

    fun observeNotificationSession(scope: CoroutineScope, clear: () -> Unit) {
        val cleanup =
            sessionCleanup
                ?: OmissionNotificationSessionCleanup(observer, clear).also { sessionCleanup = it }
        cleanup.start(scope)
    }
}
