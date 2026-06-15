package com.ikoro.android.domain.wallet

import com.ikoro.android.data.model.Asset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.utils.Convert
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
        val chainAssets = EvmChain.entries.mapIndexed { index, chain ->
            val balanceResult = when (chain) {
                EvmChain.ROOTSTOCK -> rootstock.getBalanceRaw(evmAddress)
                else -> multiEvm.balanceForChain(chain, evmAddress)
            }
            val balanceEth = balanceResult.map { Convert.fromWei(it.toBigDecimal(), Convert.Unit.ETHER) }
            val balanceStr = balanceEth.getOrNull()?.let {
                "%.6f ${chain.currency}".format(it)
            } ?: "0 ${chain.currency}"
            val fiat = balanceEth.getOrNull()?.let {
                // placeholder conversion: 1 RBTC ≈ $80k, 1 ETH ≈ $3k, etc.
                val rate = when (chain) {
                    EvmChain.ROOTSTOCK -> 80_000.0
                    EvmChain.ETHEREUM, EvmChain.BASE, EvmChain.ARBITRUM, EvmChain.OPTIMISM -> 3_000.0
                    EvmChain.POLYGON -> 0.5
                    EvmChain.BSC -> 600.0
                }
                "$%.2f".format(it.toDouble() * rate)
            } ?: "$0.00"
            Asset(
                id = chain.name.lowercase(),
                chainName = chain.chainName,
                symbol = chain.currency,
                address = evmAddress,
                balance = balanceStr,
                fiatValue = fiat,
                accentColor = chainColor(chain),
                isPrimary = index == 0
            )
        }
        chainAssets + breezAssetsList(evmAddress)
    }

    private fun breezAssetsList(evmAddress: String): List<Asset> {
        return listOf(
            Asset(
                id = "btc",
                chainName = "Bitcoin",
                symbol = "BTC",
                address = evmAddress,
                balance = "0 BTC",
                fiatValue = "$0.00",
                accentColor = 0xFFF7931A
            ),
            Asset(
                id = "lightning",
                chainName = "Lightning",
                symbol = "sats",
                address = evmAddress,
                balance = "0 sats",
                fiatValue = "$0.00",
                accentColor = 0xFFFFE600
            )
        )
    }

    fun sendToken(chain: String, credentials: org.web3j.crypto.Credentials, to: String, amount: String): Result<String> {
        Timber.i("Send %s %s on %s", amount, to, chain)
        return Result.success("tx_hash_placeholder")
    }

    suspend fun send(chainId: String, credentials: org.web3j.crypto.Credentials, to: String, amount: String): Result<String> {
        return when (chainId) {
            "rootstock" -> rootstock.sendRBTC(credentials, to, java.math.BigDecimal(amount))
            else -> Result.failure(Exception("Send not implemented for $chainId"))
        }
    }

    @Deprecated("Use loadAssets instead")
    fun listAssets(): List<String> {
        return listOf("RBTC", "L-USDT", "BTC", "USDC", "DAI") + multiEvm.allChains().map { it.currency }
    }

    fun breezAssets(): List<String> = breez.supportedAssets()
    fun evmChains(): List<EvmChain> = multiEvm.allChains()
}

private fun chainColor(chain: EvmChain): Long = when (chain) {
    EvmChain.ROOTSTOCK -> 0xFF00A36C
    EvmChain.ETHEREUM -> 0xFF627EEA
    EvmChain.BASE -> 0xFF0052FF
    EvmChain.POLYGON -> 0xFF8247E5
    EvmChain.ARBITRUM -> 0xFF28A0F0
    EvmChain.OPTIMISM -> 0xFFFF0420
    EvmChain.BSC -> 0xFFF0B90B
}
