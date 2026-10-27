package com.vitahealth.tata.app

import android.app.Application
import android.content.Context
import com.vitahealth.tata.BuildConfig
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.omission.application.ports.OmissionPushReceiver
import com.vitahealth.tata.omission.application.ports.PushTokenChangeReceiver
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.presentation.notifications.ui.OmissionNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Owns process-scoped dependencies and recovery collectors independently of screen lifecycles. */
class TataApplication : Application(), PushTokenChangeReceiver, OmissionPushReceiver {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.withSpanishAppLocale())
    }

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, BuildConfig.API_BASE_URL)
        container.observeSessionChanges(applicationScope)
        container.observeOmissionNotificationSession(applicationScope) {
            OmissionNotifications.clearDisplayed(this)
        }
        // Channels exist before the first push, also for messages the system shows in the
        // background.
        OmissionNotifications.ensureChannels(this)
    }

    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }

    /** Routes SDK token updates into the process-owned registration lifecycle. */
    override fun onPushTokenChanged(token: String) = container.onPushTokenChanged(token)

    override fun onOmissionPushReceived(push: OmissionPush) {
        if (container.canDisplayOmissionPush(push)) OmissionNotifications.show(this, push)
    }
}
