package com.vitahealth.tata.app

import android.app.Application
import com.vitahealth.tata.app.di.AppContainer

class TataApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
