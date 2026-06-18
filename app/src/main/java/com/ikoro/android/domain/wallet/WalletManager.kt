package com.ikoro.android.domain.wallet

import android.content.Context
import com.ikoro.android.data.model.Asset
import com.ikoro.android.data.remote.CoinGeckoService
import com.ikoro.android.data.remote.EvmRpcService
import com.ikoro.android.data.remote.MempoolService
import com.ikoro.android.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import wallet.core.java.AnySigner
import wallet.core.jni.BitcoinScript
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Ethereum
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class WalletManager(context: Context) {

    private val identityManager = ServiceLocator.identityManager(context)
    private val mempool = MempoolService()
    private val evmRpc = EvmRpcService()
    private val prices = CoinGeckoService()

    private val hdWallet: HDWallet?
        get() {
            val mnemonic = identityManager.loadMnemonic() ?: return null
            return try {
                HDWallet(mnemonic, "")
            } catch (e: Throwable) {
                Timber.e(e, "HDWallet native derivation failed")
                null
            }
        }

    private val chains = listOf(
        Chain("rootstock", "Rootstock", "RBTC", CoinType.ETHEREUM, 18, 0xFF00A36C, 30L),
        Chain("ethereum", "Ethereum", "ETH", CoinType.ETHEREUM, 18, 0xFF627EEA, 1L),
        Chain("base", "Base", "ETH", CoinType.ETHEREUM, 18, 0xFF0052FF, 8453L),
        Chain("polygon", "Polygon", "MATIC", CoinType.ETHEREUM, 18, 0xFF8247E5, 137L),
        Chain("arbitrum", "Arbitrum", "ETH", CoinType.ETHEREUM, 18, 0xFF28A0F0, 42161L),
        Chain("optimism", "Optimism", "ETH", CoinType.ETHEREUM, 18, 0xFFFF0420, 10L),
        Chain("bsc", "BSC", "BNB", CoinType.ETHEREUM, 18, 0xFFF0B90B, 56L)
    )

    fun initialize() {
        Timber.i("WalletManager initialized; native bridge available=${NativeWalletBridge.isAvailable()}")
    }

    suspend fun loadAssets(): List<Asset> = withContext(Dispatchers.IO) {
        if (!NativeWalletBridge.isAvailable()) {
            Timber.w("Cannot load assets: native wallet bridge unavailable")
            return@withContext listOf(
                Asset(
                    id = "native-unavailable",
                    chainName = "Wallet library unavailable",
                    symbol = "N/A",
                    address = "",
                    balance = "Native wallet library failed to load",
                    fiatValue = "",
                    accentColor = 0xFFFF0000,
                    isPrimary = false
                )
            )
        }
        val wallet = hdWallet ?: return@withContext emptyList()
        val evmAddress = wallet.getAddressForCoin(CoinType.ETHEREUM)
        val bitcoinAddress = try {
            wallet.getAddressForCoin(CoinType.BITCOIN)
        } catch (e: Throwable) {
            Timber.e(e, "Bitcoin address derivation failed")
            ""
        }

        val evmAssets = chains.map { chain ->
            async {
                try {
                    val balanceResult = evmRpc.getBalance(chain.id, evmAddress)
                    val balance = balanceResult.getOrDefault(BigDecimal.ZERO)
                    val priceResult = prices.getPriceUsd(chain.id)
                    val price = priceResult.getOrDefault(BigDecimal.ZERO)
                    val fiat = balance.multiply(price).setScale(2, RoundingMode.HALF_UP)
                    Asset(
                        id = chain.id,
                        chainName = chain.displayName,
                        symbol = chain.symbol,
                        address = evmAddress,
                        balance = "${balance.stripTrailingZeros().toPlainString()} ${chain.symbol}",
                        fiatValue = "\u0024$fiat",
                        accentColor = chain.color,
                        isPrimary = chain.id == "rootstock"
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Asset load failed for ${chain.id}")
                    Asset(
                        id = chain.id,
                        chainName = chain.displayName,
                        symbol = chain.symbol,
                        address = evmAddress,
                        balance = "Balance unavailable",
                        fiatValue = "",
                        accentColor = chain.color,
                        isPrimary = chain.id == "rootstock"
                    )
                }
            }
        }
        val btcAsset = async {
            try {
                val balanceResult = mempool.getBalance(bitcoinAddress)
                val balance = balanceResult.getOrDefault(BigDecimal.ZERO)
                val priceResult = prices.getPriceUsd("bitcoin")
                val price = priceResult.getOrDefault(BigDecimal.ZERO)
                val fiat = balance.multiply(price).setScale(2, RoundingMode.HALF_UP)
                Asset(
                    id = "bitcoin",
                    chainName = "Bitcoin",
                    symbol = "BTC",
                    address = bitcoinAddress,
                    balance = "${balance.stripTrailingZeros().toPlainString()} BTC",
                    fiatValue = "\u0024$fiat",
                    accentColor = 0xFFF7931A,
                    isPrimary = false
                )
            } catch (e: Throwable) {
                Timber.e(e, "Bitcoin asset load failed")
                Asset(
                    id = "bitcoin",
                    chainName = "Bitcoin",
                    symbol = "BTC",
                    address = bitcoinAddress,
                    balance = "Balance unavailable",
                    fiatValue = "",
                    accentColor = 0xFFF7931A,
                    isPrimary = false
                )
            }
        }
        (evmAssets + btcAsset).awaitAll().filter { it.address.isNotBlank() || it.id == "native-unavailable" }
    }

    suspend fun send(assetId: String, to: String, amount: String): Result<String> = withContext(Dispatchers.IO) {
        NativeWalletBridge.check().onFailure { return@withContext Result.failure(it) }
        try {
            require(to.isNotBlank()) { "Recipient address is required" }
            val decimal = amount.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid amount")
            val wallet = hdWallet ?: throw IllegalStateException("No wallet")

            when (assetId) {
                "bitcoin" -> sendBitcoin(wallet, to, decimal)
                else -> {
                    val chain = chains.find { it.id == assetId } ?: throw IllegalArgumentException("Unknown chain $assetId")
                    sendEvm(wallet, chain, to, decimal)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Send failed")
            Result.failure(e)
        }
    }

    private suspend fun sendEvm(wallet: HDWallet, chain: Chain, to: String, amount: BigDecimal): Result<String> {
        val privateKey = wallet.getKey(CoinType.ETHEREUM, "m/44'/60'/0'/0/0")
        val wei = amount.movePointRight(chain.decimals).toBigInteger()
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = chain.chainId().toByteArray().toByteString()
            this.gasPrice = BigInteger("20000000000").toByteArray().toByteString()
            this.gasLimit = BigInteger("21000").toByteArray().toByteString()
            this.toAddress = to
            this.transaction = Ethereum.Transaction.newBuilder().apply {
                transfer = Ethereum.Transaction.Transfer.newBuilder().apply {
                    this.amount = wei.toByteArray().toByteString()
                }.build()
            }.build()
            this.privateKey = privateKey.data().toByteString()
        }.build()
        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        val signed = output.encoded.toByteArray().toHexString()
        return evmRpc.sendRawTransaction(chain.id, signed)
    }

    private suspend fun sendBitcoin(wallet: HDWallet, to: String, amount: BigDecimal): Result<String> {
        val address = wallet.getAddressForCoin(CoinType.BITCOIN)
        val privateKey = wallet.getKey(CoinType.BITCOIN, "m/84'/0'/0'/0/0")
        val utxos = mempool.getUtxos(address).getOrElse { return Result.failure(it) }
        val satoshiAmount = amount.movePointRight(8).toLong()
        val lockScript = BitcoinScript.lockScriptForAddress(address, CoinType.BITCOIN)
        val inputs = utxos.map { utxo ->
            Bitcoin.UnspentTransaction.newBuilder().apply {
                this.outPoint = Bitcoin.OutPoint.newBuilder().apply {
                    this.hash = utxo.txid.hexToBytesReversed().toByteString()
                    this.index = utxo.vout
                }.build()
                this.amount = utxo.value
                this.script = lockScript.data().toByteString()
            }.build()
        }
        val signingInput = Bitcoin.SigningInput.newBuilder().apply {
            this.hashType = BitcoinScript.hashTypeForCoin(CoinType.BITCOIN)
            this.amount = satoshiAmount
            this.byteFee = 10
            this.toAddress = to
            this.changeAddress = address
            this.coinType = CoinType.BITCOIN.value()
            this.addAllUtxo(inputs)
            this.addPrivateKey(privateKey.data().toByteString())
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.BITCOIN, Bitcoin.SigningOutput.parser())
        val txHex = output.encoded.toByteArray().toHexString()
        return mempool.broadcastTransaction(txHex)
    }

    private data class Chain(
        val id: String,
        val displayName: String,
        val symbol: String,
        val coinType: CoinType,
        val decimals: Int,
        val color: Long,
        val chainId: Long
    )

    private fun Chain.chainId(): Long = chainId
}

private fun Long.toByteArray(): ByteArray = BigInteger.valueOf(this).toByteArray()
private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
private fun String.hexToBytesReversed(): ByteArray {
    val bytes = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    return bytes.reversedArray()
}
private fun ByteArray.toByteString() = com.google.protobuf.ByteString.copyFrom(this)
