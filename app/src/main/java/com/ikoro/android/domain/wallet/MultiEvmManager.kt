package com.ikoro.android.domain.wallet

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import timber.log.Timber
import java.math.BigInteger

class MultiEvmManager {

    fun balanceForChain(chain: EvmChain, address: String): Result<BigInteger> {
        return try {
            val web3 = Web3j.build(HttpService(chain.rpcUrl))
            val balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            Result.success(balance.balance)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch %s balance", chain.chainName)
            Result.failure(e)
        }
    }

    fun allChains(): List<EvmChain> = EvmChain.entries
}
