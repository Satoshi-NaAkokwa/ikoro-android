package com.ikoro.android.data.remote

import com.ikoro.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber

class LiveKitCallManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun fetchToken(room: String, identity: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = BuildConfig.LIVEKIT_TOKEN_ENDPOINT
            if (endpoint.isBlank()) {
                return@withContext Result.failure(IllegalStateException("LiveKit token endpoint not configured"))
            }
            val body = JSONObject().apply {
                put("room", room)
                put("identity", identity)
            }.toString()
            val requestBody = body.toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}: $responseBody"))
            val json = JSONObject(responseBody)
            Result.success(json.getString("token"))
        } catch (e: Exception) {
            Timber.e(e, "LiveKit token fetch failed")
            Result.failure(e)
        }
    }

    fun connectUrl(): String = BuildConfig.LIVEKIT_URL
}
