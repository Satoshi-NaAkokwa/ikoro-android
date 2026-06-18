package com.ikoro.android.domain.wallet

import timber.log.Timber
import wallet.core.jni.HDWallet

object NativeWalletBridge {

    @Volatile
    private var available: Boolean? = null

    fun isAvailable(): Boolean {
        available?.let { return it }
        return synchronized(this) {
            available ?: try {
                // Any cheap operation that forces JNI class init + native load
                HDWallet(256, "")
                Timber.i("TrustWalletCore native bridge available")
                true
            } catch (e: Throwable) {
                Timber.e(e, "TrustWalletCore native bridge unavailable")
                false
            }.also { available = it }
        }
    }

    fun check(): Result<Unit> {
        return if (isAvailable()) Result.success(Unit)
        else Result.failure(IllegalStateException("Native wallet library is not available on this device. Signing and address derivation are disabled."))
    }
}
