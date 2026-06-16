package com.ikoro.android.app

import android.app.Application
import com.ikoro.android.BuildConfig
import com.ikoro.android.data.remote.NtfyService
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.domain.identity.IdentityManager
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
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
            ServiceLocator.init(this)
            startNtfySubscription()
            Timber.i("IkoroApplication started safely.")
        } catch (t: Throwable) {
            // Never crash on startup. Degraded mode is better than dead app.
            Timber.e(t, "Critical failure during application startup.")
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
