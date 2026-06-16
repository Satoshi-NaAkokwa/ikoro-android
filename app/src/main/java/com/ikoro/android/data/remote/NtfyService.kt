package com.ikoro.android.data.remote

import com.ikoro.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSources
import okhttp3.sse.EventSourceListener
import timber.log.Timber

class NtfyService(private val client: OkHttpClient = OkHttpClient()) {

    private val jsonMediaType = "application/json".toMediaType()

    fun topicForIdentity(seedFingerprint: String): String = "ikoro-${seedFingerprint.take(16)}"

    suspend fun publish(topic: String, title: String, message: String, priority: Int = 3): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${BuildConfig.NTFY_SERVER_URL}/$topic"
            val body = """{"topic":"$topic","title":"$title","message":"$message","priority":$priority}"""
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${response.code}: $responseBody"))
            Result.success(responseBody)
        } catch (e: Exception) {
            Timber.e(e, "ntfy publish failed")
            Result.failure(e)
        }
    }

    fun subscribe(topic: String, onMessage: (String) -> Unit): EventSource {
        val url = "${BuildConfig.NTFY_SERVER_URL}/$topic/sse"
        val request = Request.Builder().url(url).build()
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                Timber.i("ntfy SSE open on $topic")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                Timber.i("ntfy event: $data")
                onMessage(data)
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                Timber.e(t, "ntfy SSE failure")
            }

            override fun onClosed(eventSource: EventSource) {
                Timber.i("ntfy SSE closed")
            }
        }
        return EventSources.createFactory(client).newEventSource(request, listener)
    }
}
