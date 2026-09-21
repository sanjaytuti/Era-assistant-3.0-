package com.example.era

import android.app.Application
import android.content.Context

class EraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContext = applicationContext
    }

    companion object {
        lateinit var instance: EraApp
            private set
        lateinit var appContext: Context
            private set
    }
}
