package com.ikoro.android.domain.wallet

import android.content.Context
import com.ikoro.android.data.model.Asset
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.domain.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import wallet.core.jni.proto.Ethereum
import java.math.BigDecimal
import java.math.BigInteger

class WalletManager(context: Context) {

    private val identityManager = ServiceLocator.identityManager(context)

    private val hdWallet: HDWallet?
        get() = identityManager.loadMnemonic()?.let { HDWallet(it, "") }

    private val chains = listOf(
        Chain("rootstock", "Rootstock", "RBTC", CoinType.ROOTSTOCK, 0xFF00A36C),
        Chain("ethereum", "Ethereum", "ETH", CoinType.ETHEREUM, 0xFF627EEA),
        Chain("base", "Base", "ETH", CoinType.BASE, 0xFF0052FF),
        Chain("polygon", "Polygon", "MATIC", CoinType.POLYGON, 0xFF8247E5),
        Chain("arbitrum", "Arbitrum", "ETH", CoinType.ARBITRUM, 0xFF28A0F0),
        Chain("optimism", "Optimism", "ETH", CoinType.OPTIMISM, 0xFFFF0420),
        Chain("bsc", "BSC", "BNB", CoinType.SMARTCHAIN, 0xFFF0B90B)
    )

    fun initialize() {
        Timber.i("WalletManager initialized with Trust Wallet Core")
    }

    fun getAddress(coin: CoinType): String? {
        return try {
            hdWallet?.getAddressForCoin(coin)
        } catch (e: Exception) {
            Timber.e(e, "Failed to derive address")
            null
        }
    }

    suspend fun loadAssets(): List<Asset> = withContext(Dispatchers.IO) {
        val wallet = hdWallet ?: return@withContext emptyList()
        chains.mapIndexed { index, chain ->
            val address = wallet.getAddressForCoin(chain.coinType)
            Asset(
                id = chain.id,
                chainName = chain.displayName,
                symbol = chain.symbol,
                address = address,
                balance = "0 ${chain.symbol}",
                fiatValue = "$0.00",
                accentColor = chain.color,
                isPrimary = index == 0
            )
        } + wallet.getAddressForCoin(CoinType.BITCOIN).let { address ->
            Asset(
                id = "bitcoin",
                chainName = "Bitcoin",
                symbol = "BTC",
                address = address,
                balance = "0 BTC",
                fiatValue = "$0.00",
                accentColor = 0xFFF7931A,
                isPrimary = false
            )
        }
    }

    suspend fun send(assetId: String, to: String, amount: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            require(to.isNotBlank()) { "Recipient address is required" }
            require(amount.toBigDecimalOrNull() != null) { "Invalid amount" }
            Timber.i("Sending $amount of $assetId to $to")
            Result.success("signed-tx-placeholder")
        } catch (e: Exception) {
            Timber.e(e, "Send failed")
            Result.failure(e)
        }
    }

    fun signEthereumTransaction(
        toAddress: String,
        amount: BigDecimal,
        chainId: Long,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        nonce: Long
    ): Result<String> {
        val wallet = hdWallet ?: return Result.failure(IllegalStateException("No wallet"))
        return try {
            val privateKey = wallet.getKey(CoinType.ETHEREUM, "m/44'/60'/0'/0/0")
            val input = Ethereum.SigningInput.newBuilder().apply {
                this.chainId = ByteString.copyFrom(chainId.toBigInteger().toByteArray())
                this.gasPrice = ByteString.copyFrom(gasPrice.toByteArray())
                this.gasLimit = ByteString.copyFrom(gasLimit.toByteArray())
                this.toAddress = toAddress
                this.transaction = Ethereum.Transaction.newBuilder().apply {
                    transfer = Ethereum.Transaction.Transfer.newBuilder().apply {
                        this.amount = ByteString.copyFrom(amount.toBigInteger().toByteArray())
                    }.build()
                }.build()
                this.privateKey = ByteString.copyFrom(privateKey.data())
            }.build()
            val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
            Result.success(output.encoded.toByteArray().toHexString())
        } catch (e: Exception) {
            Timber.e(e, "Failed to sign Ethereum transaction")
            Result.failure(e)
        }
    }

    private data class Chain(
        val id: String,
        val displayName: String,
        val symbol: String,
        val coinType: CoinType,
        val color: Long
    )
}

private fun BigInteger.toByteArray(): ByteArray = this.toByteArray()
private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
private object ByteString {
    fun copyFrom(data: ByteArray) = com.google.protobuf.ByteString.copyFrom(data)
}
