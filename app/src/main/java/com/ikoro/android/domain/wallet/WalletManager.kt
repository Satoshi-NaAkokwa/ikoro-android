package com.ikoro.android.domain.wallet

import timber.log.Timber

class WalletManager {

    private val breez = BreezWalletManager()
    private val rootstock = RootstockManager()
    private val multiEvm = MultiEvmManager()

    fun initialize() {
        breez.initialize()
        Timber.i("WalletManager initialized; Rootstock RPC=%s", rootstock.rpcUrl())
    }

    fun rootstockRpc(): String = rootstock.rpcUrl()

    fun getBalance(): Result<String> {
        return Result.success("0 RBTC")
    }

    fun sendToken(chain: String, to: String, amount: String): Result<String> {
        Timber.i("Send %s %s on %s", amount, to, chain)
        return Result.success("tx_hash_placeholder")
    }

    fun listAssets(): List<String> {
        return listOf("RBTC", "L-USDT", "BTC", "USDC", "DAI") + multiEvm.allChains().map { it.currency }
    }

    fun breezAssets(): List<String> = breez.supportedAssets()
    fun evmChains(): List<EvmChain> = multiEvm.allChains()
}
