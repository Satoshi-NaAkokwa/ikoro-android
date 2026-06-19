package com.ikoro.android.di

import android.content.Context
import com.ikoro.android.data.local.IdentityStore
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.domain.wallet.TrustWalletDerivation
import com.ikoro.android.domain.wallet.WalletDerivation
import com.ikoro.android.domain.wallet.WalletManager
import timber.log.Timber

object ServiceLocator {

    private var appContext: Context? = null

    @Volatile
    private var identityStore: IdentityStore? = null

    @Volatile
    private var identityManager: IdentityManager? = null

    @Volatile
    private var walletDerivation: WalletDerivation? = null

    @Volatile
    private var walletManager: WalletManager? = null

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        Timber.plant(Timber.DebugTree())
    }

    fun identityStore(context: Context): IdentityStore {
        return identityStore ?: synchronized(this) {
            identityStore ?: IdentityStore(context).also { identityStore = it }
        }
    }

    private fun walletDerivation(): WalletDerivation {
        return walletDerivation ?: synchronized(this) {
            walletDerivation ?: TrustWalletDerivation().also { walletDerivation = it }
        }
    }

    fun identityManager(context: Context): IdentityManager {
        return identityManager ?: synchronized(this) {
            identityManager ?: IdentityManager(identityStore(context), walletDerivation()).also {
                identityManager = it
            }
        }
    }

    fun walletManager(identityManager: IdentityManager): WalletManager {
        return walletManager ?: synchronized(this) {
            walletManager ?: WalletManager(identityManager).also { walletManager = it }
        }
    }
}
