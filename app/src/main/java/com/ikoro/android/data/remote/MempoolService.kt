package com.ikoro.android.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal

class MempoolService(private val client: OkHttpClient = OkHttpClient()) {

    private val textMediaType = "text/plain".toMediaType()

    suspend fun getBalance(address: String): Result<BigDecimal> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://mempool.space/api/address/$address")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
            val json = JSONObject(body)
            val chainStats = json.getJSONObject("chain_stats")
            val funded = chainStats.getLong("funded_txo_sum")
            val spent = chainStats.getLong("spent_txo_sum")
            val balance = BigDecimal.valueOf(funded - spent).movePointLeft(8)
            Result.success(balance)
        } catch (e: Exception) {
            Timber.e(e, "Mempool balance fetch failed for $address")
            Result.failure(e)
        }
    }

    suspend fun getUtxos(address: String): Result<List<Utxo>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://mempool.space/api/address/$address/utxo")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
            val array = org.json.JSONArray(body)
            val utxos = (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                Utxo(
                    txid = obj.getString("txid"),
                    vout = obj.getInt("vout"),
                    value = obj.getLong("value"),
                    scriptPubKey = obj.getString("scriptpubkey")
                )
            }
            Result.success(utxos)
        } catch (e: Exception) {
            Timber.e(e, "Mempool UTXO fetch failed for $address")
            Result.failure(e)
        }
    }

    suspend fun broadcastTransaction(hex: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://mempool.space/api/tx")
                .post(hex.toRequestBody(textMediaType))
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}: $body"))
            Result.success(body)
        } catch (e: Exception) {
            Timber.e(e, "Mempool broadcast failed")
            Result.failure(e)
        }
    }

    data class Utxo(
        val txid: String,
        val vout: Int,
        val value: Long,
        val scriptPubKey: String
    )
}
