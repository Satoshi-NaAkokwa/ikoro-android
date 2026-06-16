package com.ikoro.android.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class EvmRpcService(private val client: OkHttpClient = OkHttpClient()) {

    private val endpoints = mapOf(
        "rootstock" to "https://public-node.rsk.co",
        "ethereum" to "https://eth.llamarpc.com",
        "base" to "https://base.llamarpc.com",
        "polygon" to "https://polygon.llamarpc.com",
        "arbitrum" to "https://arbitrum.llamarpc.com",
        "optimism" to "https://optimism.llamarpc.com",
        "bsc" to "https://binance.llamarpc.com"
    )

    private val jsonMediaType = "application/json".toMediaType()

    suspend fun getBalance(chainId: String, address: String): Result<BigDecimal> = withContext(Dispatchers.IO) {
        try {
            val endpoint = endpoints[chainId] ?: return@withContext Result.failure(IllegalArgumentException("Unknown chain $chainId"))
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "eth_getBalance")
                put("params", org.json.JSONArray().put(address).put("latest"))
            }
            val request = Request.Builder()
                .url(endpoint)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
            val json = JSONObject(responseBody)
            if (json.has("error")) return@withContext Result.failure(IllegalStateException(json.getJSONObject("error").getString("message")))
            val hex = json.getString("result").removePrefix("0x")
            val wei = BigDecimal(BigInteger(hex, 16))
            val balance = wei.movePointLeft(18)
            Result.success(balance)
        } catch (e: Exception) {
            Timber.e(e, "EVM balance fetch failed for $chainId/$address")
            Result.failure(e)
        }
    }

    suspend fun sendRawTransaction(chainId: String, signedTxHex: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = endpoints[chainId] ?: return@withContext Result.failure(IllegalArgumentException("Unknown chain $chainId"))
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "eth_sendRawTransaction")
                put("params", org.json.JSONArray().put(signedTxHex))
            }
            val request = Request.Builder()
                .url(endpoint)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
            val json = JSONObject(responseBody)
            if (json.has("error")) return@withContext Result.failure(IllegalStateException(json.getJSONObject("error").getString("message")))
            Result.success(json.getString("result"))
        } catch (e: Exception) {
            Timber.e(e, "EVM broadcast failed for $chainId")
            Result.failure(e)
        }
    }
}
