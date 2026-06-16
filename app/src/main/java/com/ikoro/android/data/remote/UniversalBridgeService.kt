package com.ikoro.android.data.remote

import com.ikoro.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.math.BigDecimal

class UniversalBridgeService(private val client: OkHttpClient = OkHttpClient()) {

    suspend fun getQuote(
        fromChainId: String,
        toChainId: String,
        fromAmount: BigDecimal,
        fromToken: String,
        toToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val clientId = BuildConfig.THIRDWEB_CLIENT_ID
        if (clientId.isBlank()) {
            return@withContext Result.failure(IllegalStateException("thirdweb clientId not configured"))
        }
        Timber.i("Universal Bridge quote: $fromChainId -> $toChainId, $fromAmount $fromToken -> $toToken")
        // TODO: call https://bridge.thirdweb.com/v1/{route} with clientId
        Result.failure(IllegalStateException("Universal Bridge not yet wired"))
    }

    suspend fun executeSwap(quoteId: String): Result<String> = withContext(Dispatchers.IO) {
        val clientId = BuildConfig.THIRDWEB_CLIENT_ID
        if (clientId.isBlank()) {
            return@withContext Result.failure(IllegalStateException("thirdweb clientId not configured"))
        }
        Timber.i("Executing Universal Bridge swap $quoteId")
        Result.failure(IllegalStateException("Universal Bridge not yet wired"))
    }
}
