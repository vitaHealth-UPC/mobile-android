package com.vitahealth.tata.app

import android.app.Application
import com.vitahealth.tata.app.di.AppContainer

class TataApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, com.vitahealth.tata.BuildConfig.API_BASE_URL)
        // Channels exist before the first push, also for messages the system shows in the background.
        com.vitahealth.tata.omission.presentation.notifications.OmissionNotifications.ensureChannels(this)
    }
}
