package com.ikoro.android.app

import android.app.Application
import com.ikoro.android.BuildConfig
import com.ikoro.android.data.remote.NtfyService
import com.ikoro.android.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
            startNtfySubscription()
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
        try {
            System.loadLibrary("nostr_sdk_ffi")
            Timber.i("Loaded nostr_sdk_ffi native library.")
        } catch (e: Throwable) {
            Timber.e(e, "Failed to load nostr_sdk_ffi")
        }
    }

    private fun startNtfySubscription() {
        val identityManager = ServiceLocator.identityManager(this)
        val identity = identityManager.loadExistingIdentity()
        if (identity != null) {
            val ntfy = NtfyService()
            val topic = ntfy.topicForIdentity(identity.seedFingerprint)
            ntfy.subscribe(topic) { data ->
                Timber.i("ntfy push received: $data")
            }
            appScope.launch {
                ntfy.publish(topic, "Ikoro ready", "Push notifications are active for this identity.")
            }
        }
    }
}
