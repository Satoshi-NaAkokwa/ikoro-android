package com.ikoro.android.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal

class CoinGeckoService(private val client: OkHttpClient = OkHttpClient()) {

    private val baseUrl = "https://api.coingecko.com/api/v3"

    private val idMap = mapOf(
        "bitcoin" to "bitcoin",
        "ethereum" to "ethereum",
        "rootstock" to "rootstock",
        "base" to "ethereum",
        "polygon" to "matic-network",
        "arbitrum" to "ethereum",
        "optimism" to "ethereum",
        "bsc" to "binancecoin"
    )

    suspend fun getPriceUsd(assetId: String): Result<BigDecimal> = withContext(Dispatchers.IO) {
        try {
            val coinId = idMap[assetId] ?: return@withContext Result.failure(IllegalArgumentException("Unknown asset $assetId"))
            val request = Request.Builder()
                .url("$baseUrl/simple/price?ids=$coinId&vs_currencies=usd")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
            val json = JSONObject(body)
            val price = json.getJSONObject(coinId).getDouble("usd")
            Result.success(BigDecimal.valueOf(price))
        } catch (e: Exception) {
            Timber.e(e, "CoinGecko price fetch failed for $assetId")
            Result.failure(e)
        }
    }
}
