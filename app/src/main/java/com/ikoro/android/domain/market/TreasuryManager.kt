package com.ikoro.android.domain.market

import timber.log.Timber

class TreasuryManager {

    fun contribute(amount: String, asset: String): Result<String> {
        Timber.i("Treasury contribution: %s %s", amount, asset)
        return Result.success("treasury_tx_placeholder")
    }

    fun balance(): String = "0"
}
