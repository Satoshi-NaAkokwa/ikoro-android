package com.ikoro.android.di

import android.content.Context
import androidx.room.Room
import com.ikoro.android.data.local.AppDatabase
import com.ikoro.android.data.local.IdentityStore
import com.ikoro.android.data.remote.EvmRpcService
import com.ikoro.android.data.remote.SimplexBridge
import com.ikoro.android.data.remote.ThirdwebContractService
import com.ikoro.android.domain.chat.ChatManager
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
    private var database: AppDatabase? = null

    @Volatile
    private var identityManager: IdentityManager? = null

    @Volatile
    private var walletDerivation: WalletDerivation? = null

    @Volatile
    private var chatManager: ChatManager? = null

    @Volatile
    private var walletManager: WalletManager? = null

    @Volatile
    private var thirdwebContractService: ThirdwebContractService? = null

    @Volatile
    private var evmRpcService: EvmRpcService? = null

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

    private fun database(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "ikoro.db"
            ).build().also { database = it }
        }
    }

    private fun walletDerivation(): WalletDerivation {
        return walletDerivation ?: synchronized(this) {
            walletDerivation ?: TrustWalletDerivation().also { walletDerivation = it }
        }
    }

    private fun evmRpcService(): EvmRpcService {
        return evmRpcService ?: synchronized(this) {
            evmRpcService ?: EvmRpcService().also { evmRpcService = it }
        }
    }

    fun identityManager(context: Context): IdentityManager {
        return identityManager ?: synchronized(this) {
            identityManager ?: IdentityManager(identityStore(context), walletDerivation()).also {
                identityManager = it
            }
        }
    }

    fun chatManager(context: Context): ChatManager {
        return chatManager ?: synchronized(this) {
            chatManager ?: ChatManager(
                context,
                SimplexBridge(),
                database(context).messageDao(),
                database(context).contactDao()
            ).also { chatManager = it }
        }
    }

    fun walletManager(context: Context): WalletManager {
        return walletManager ?: synchronized(this) {
            walletManager ?: WalletManager(context).also { walletManager = it }
        }
    }

    fun thirdwebContractService(context: Context): ThirdwebContractService {
        return thirdwebContractService ?: synchronized(this) {
            thirdwebContractService ?: ThirdwebContractService(
                evmRpcService(),
                walletManager(context)
            ).also { thirdwebContractService = it }
        }
    }
}
