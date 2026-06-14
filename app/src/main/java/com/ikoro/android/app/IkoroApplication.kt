package com.ikoro.android.app

import android.app.Application
import com.ikoro.android.BuildConfig
import com.ikoro.android.di.ServiceLocator
import timber.log.Timber

class IkoroApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
            ServiceLocator.init(this)
            Timber.i("IkoroApplication started safely.")
        } catch (t: Throwable) {
            // Never crash on startup. Degraded mode is better than dead app.
            Timber.e(t, "Critical failure during application startup.")
        }
    }
}
