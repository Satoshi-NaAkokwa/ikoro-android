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
        get() = identityManager.loadMnemonic()?.let { HDWallet(it, "") }

    private val chains = listOf(
        Chain("rootstock", "Rootstock", "RBTC", CoinType.ROOTSTOCK, 18, 0xFF00A36C),
        Chain("ethereum", "Ethereum", "ETH", CoinType.ETHEREUM, 18, 0xFF627EEA),
        Chain("base", "Base", "ETH", CoinType.BASE, 18, 0xFF0052FF),
        Chain("polygon", "Polygon", "MATIC", CoinType.POLYGON, 18, 0xFF8247E5),
        Chain("arbitrum", "Arbitrum", "ETH", CoinType.ARBITRUM, 18, 0xFF28A0F0),
        Chain("optimism", "Optimism", "ETH", CoinType.OPTIMISM, 18, 0xFFFF0420),
        Chain("bsc", "BSC", "BNB", CoinType.SMARTCHAIN, 18, 0xFFF0B90B)
    )

    fun initialize() {
        Timber.i("WalletManager initialized with Trust Wallet Core")
    }

    suspend fun loadAssets(): List<Asset> = withContext(Dispatchers.IO) {
        val wallet = hdWallet ?: return@withContext emptyList()
        val all = chains.map { chain ->
            async {
                val address = wallet.getAddressForCoin(chain.coinType)
                val balanceResult = evmRpc.getBalance(chain.id, address)
                val balance = balanceResult.getOrDefault(BigDecimal.ZERO)
                val priceResult = prices.getPriceUsd(chain.id)
                val price = priceResult.getOrDefault(BigDecimal.ZERO)
                val fiat = balance.multiply(price).setScale(2, RoundingMode.HALF_UP)
                Asset(
                    id = chain.id,
                    chainName = chain.displayName,
                    symbol = chain.symbol,
                    address = address,
                    balance = "${balance.stripTrailingZeros().toPlainString()} ${chain.symbol}",
                    fiatValue = "\u0024$fiat",
                    accentColor = chain.color,
                    isPrimary = chain.id == "rootstock"
                )
            }
        } + async {
            val address = wallet.getAddressForCoin(CoinType.BITCOIN)
            val balanceResult = mempool.getBalance(address)
            val balance = balanceResult.getOrDefault(BigDecimal.ZERO)
            val priceResult = prices.getPriceUsd("bitcoin")
            val price = priceResult.getOrDefault(BigDecimal.ZERO)
            val fiat = balance.multiply(price).setScale(2, RoundingMode.HALF_UP)
            Asset(
                id = "bitcoin",
                chainName = "Bitcoin",
                symbol = "BTC",
                address = address,
                balance = "${balance.stripTrailingZeros().toPlainString()} BTC",
                fiatValue = "\u0024$fiat",
                accentColor = 0xFFF7931A,
                isPrimary = false
            )
        }
        all.awaitAll().filter { it.address.isNotBlank() }
    }

    suspend fun send(assetId: String, to: String, amount: String): Result<String> = withContext(Dispatchers.IO) {
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
        val privateKey = wallet.getKey(chain.coinType, "m/44'/60'/0'/0/0")
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
        val output = AnySigner.sign(input, chain.coinType, Ethereum.SigningOutput.parser())
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
        val color: Long
    ) {
        fun chainId(): Long {
            return when (coinType) {
                CoinType.ETHEREUM -> 1L
                CoinType.ROOTSTOCK -> 30L
                CoinType.BASE -> 8453L
                CoinType.POLYGON -> 137L
                CoinType.ARBITRUM -> 42161L
                CoinType.OPTIMISM -> 10L
                CoinType.SMARTCHAIN -> 56L
                else -> 1L
            }
        }
    }
}

private fun Long.toByteArray(): ByteArray = BigInteger.valueOf(this).toByteArray()
private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
private fun String.hexToBytesReversed(): ByteArray {
    val bytes = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    return bytes.reversedArray()
}
private fun ByteArray.toByteString() = com.google.protobuf.ByteString.copyFrom(this)
