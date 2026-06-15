package com.ikoro.android.domain.wallet

import com.ikoro.android.data.model.Asset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun loadAssets(evmAddress: String): List<Asset> = withContext(Dispatchers.IO) {
        val chains = listOf(
            Asset(
                id = "rootstock",
                chainName = "Rootstock",
                symbol = "RBTC",
                address = evmAddress,
                accentColor = 0xFF00A36C,
                isPrimary = true
            ),
            Asset(
                id = "ethereum",
                chainName = "Ethereum",
                symbol = "ETH",
                address = evmAddress,
                accentColor = 0xFF627EEA
            ),
            Asset(
                id = "base",
                chainName = "Base",
                symbol = "ETH",
                address = evmAddress,
                accentColor = 0xFF0052FF
            ),
            Asset(
                id = "polygon",
                chainName = "Polygon",
                symbol = "MATIC",
                address = evmAddress,
                accentColor = 0xFF8247E5
            ),
            Asset(
                id = "arbitrum",
                chainName = "Arbitrum",
                symbol = "ETH",
                address = evmAddress,
                accentColor = 0xFF28A0F0
            ),
            Asset(
                id = "optimism",
                chainName = "Optimism",
                symbol = "ETH",
                address = evmAddress,
                accentColor = 0xFFFF0420
            ),
            Asset(
                id = "bsc",
                chainName = "BSC",
                symbol = "BNB",
                address = evmAddress,
                accentColor = 0xFFF0B90B
            )
        )

        // Try to load Rootstock balance as MVP real data
        val rbtcBalance = rootstock.getBalance(evmAddress)
        chains.map { asset ->
            if (asset.id == "rootstock" && rbtcBalance.isSuccess) {
                asset.copy(balance = rbtcBalance.getOrThrow())
            } else {
                asset
            }
        }
    }

    fun getBalance(): Result<String> {
        return Result.success("0 RBTC")
    }

    fun sendToken(chain: String, to: String, amount: String): Result<String> {
        Timber.i("Send %s %s on %s", amount, to, chain)
        return Result.success("tx_hash_placeholder")
    }

    @Deprecated("Use loadAssets instead")
    fun listAssets(): List<String> {
        return listOf("RBTC", "L-USDT", "BTC", "USDC", "DAI") + multiEvm.allChains().map { it.currency }
    }

    fun breezAssets(): List<String> = breez.supportedAssets()
    fun evmChains(): List<EvmChain> = multiEvm.allChains()
}
