package com.ikoro.android.domain.wallet

import com.ikoro.android.BuildConfig
import timber.log.Timber

class BreezWalletManager {

    fun initialize() {
        Timber.i("BreezWalletManager initialized; API key present=%s", BuildConfig.BREEZ_API_KEY.isNotBlank())
    }

    fun supportedAssets(): List<String> {
        return listOf("BTC", "L-USDT")
    }

    fun getBalance(): Result<String> {
        return Result.success("0 BTC (Breez Liquid)")
    }
}
