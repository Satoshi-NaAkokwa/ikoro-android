package com.ikoro.android.domain.wallet

import com.ikoro.android.BuildConfig
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class RootstockManager {

    private val web3: Web3j by lazy {
        Web3j.build(HttpService(BuildConfig.ROOTSTOCK_RPC))
    }

    fun rpcUrl(): String = BuildConfig.ROOTSTOCK_RPC

    fun getBalance(address: String): Result<String> {
        return try {
            val ethBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            val rbtc = org.web3j.utils.Convert.fromWei(ethBalance.balance.toBigDecimal(), org.web3j.utils.Convert.Unit.ETHER)
            Result.success(String.format("%.6f RBTC", rbtc))
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch Rootstock balance")
            Result.failure(e)
        }
    }

    fun getBalanceRaw(address: String): Result<BigInteger> {
        return try {
            val ethBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            Result.success(ethBalance.balance)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch Rootstock balance")
            Result.failure(e)
        }
    }

    fun chainId(): Long = 30L

    suspend fun sendRBTC(
        credentials: Credentials,
        toAddress: String,
        amountEth: BigDecimal
    ): Result<String> {
        return try {
            val to = toAddress.removePrefix("0x")
            val gasPrice = web3.ethGasPrice().send().gasPrice
            val nonce = web3.ethGetTransactionCount(
                credentials.address,
                DefaultBlockParameterName.LATEST
            ).send().transactionCount
            val value = Convert.toWei(amountEth, Convert.Unit.ETHER).toBigIntegerExact()
            val gasLimit = BigInteger.valueOf(21_000)
            val raw = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value
            )
            val signed = TransactionEncoder.signMessage(raw, chainId(), credentials)
            val hex = Numeric.toHexString(signed)
            val response = web3.ethSendRawTransaction(hex).send()
            if (response.hasError()) {
                return Result.failure(Exception(response.error.message))
            }
            Result.success(response.transactionHash)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send RBTC")
            Result.failure(e)
        }
    }
}
