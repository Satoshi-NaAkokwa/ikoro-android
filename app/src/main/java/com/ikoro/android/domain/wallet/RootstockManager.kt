package com.ikoro.android.domain.wallet

import com.ikoro.android.BuildConfig
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import timber.log.Timber
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
}
