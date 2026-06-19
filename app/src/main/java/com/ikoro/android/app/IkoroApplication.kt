package com.ikoro.android.app

import android.app.Application
import com.ikoro.android.BuildConfig
import com.ikoro.android.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class IkoroApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        try {
            loadNativeLibraries()
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
            ServiceLocator.init(this)
            Timber.i("IkoroApplication started safely.")
        } catch (t: Throwable) {
            Timber.e(t, "Critical failure during application startup.")
        }
    }

    private fun loadNativeLibraries() {
        try {
            System.loadLibrary("TrustWalletCore")
            Timber.i("Loaded TrustWalletCore native library.")
        } catch (e: Throwable) {
            Timber.e(e, "Failed to load TrustWalletCore")
        }
    }
}
