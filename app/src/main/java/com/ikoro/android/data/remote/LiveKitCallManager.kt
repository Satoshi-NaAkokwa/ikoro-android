package com.ikoro.android.data.remote

import android.content.Context
import com.ikoro.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber

class LiveKitCallManager(context: Context) {

    private val client = OkHttpClient()

    suspend fun fetchToken(room: String, identity: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = BuildConfig.LIVEKIT_TOKEN_ENDPOINT
            if (endpoint.isBlank()) {
                return@withContext Result.failure(IllegalStateException("LiveKit token endpoint not configured"))
            }
            val url = "$endpoint?room=${java.net.URLEncoder.encode(room, "UTF-8")}&identity=${java.net.URLEncoder.encode(identity, "UTF-8")}"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response"))
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}: $body"))
            val json = JSONObject(body)
            Result.success(json.getString("token"))
        } catch (e: Exception) {
            Timber.e(e, "LiveKit token fetch failed")
            Result.failure(e)
        }
    }

    fun connectUrl(): String = BuildConfig.LIVEKIT_URL
}
