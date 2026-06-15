package com.ikoro.android.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rust.nostr.sdk.Client
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Keys
import rust.nostr.sdk.NostrSigner
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayUrl
import rust.nostr.sdk.SecretKey
import rust.nostr.sdk.Timestamp
import timber.log.Timber
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Lightweight Nostr client for the user's private channel to Agbara.
 * Uses rust-nostr KMP under the hood. Messages are NIP-17 private DMs
 * (sealed/gift-wrap) and only readable by the two participants.
 */
class NostrDmClient(userNsec: String) : AutoCloseable {

    private val userKeys: Keys = Keys(SecretKey.parse(userNsec))
    private val signer: NostrSigner = NostrSigner.keys(userKeys)
    private val client: Client = Client(signer)
    private val relays = mutableListOf<RelayUrl>()

    fun addRelays(urls: List<String>) {
        urls.forEach { url ->
            try {
                val relay = RelayUrl.parse(url)
                relays.add(relay)
            } catch (e: Exception) {
                Timber.w("Invalid relay URL: %s", url)
            }
        }
    }

    suspend fun connect() {
        withContext(Dispatchers.IO) {
            relays.forEach { relay ->
                try {
                    client.addRelay(relay)
                } catch (e: Exception) {
                    Timber.w("Failed to add relay %s: %s", relay, e.message)
                }
            }
            try {
                client.connect()
            } catch (e: Exception) {
                Timber.e("Nostr client connect failed: %s", e.message)
            }
        }
    }

    suspend fun sendPrivateMessage(recipientNpub: String, text: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val recipient = PublicKey.parse(recipientNpub)
                val output = client.sendPrivateMsg(recipient, text, emptyList())
                val id = output.id.toHex()
                Timber.i("Sent Nostr DM to %s: %s", recipientNpub, id)
                Result.success(id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send Nostr DM")
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch recent private DMs where the user is involved.
     * Returns raw event content strings (caller must decrypt if needed).
     */
    suspend fun fetchRecentDms(hoursBack: Long = 24): Result<List<Pair<String, String>>> {
        return withContext(Dispatchers.IO) {
            try {
                val kind = Kind.fromStd(KindStandard.PRIVATE_DIRECT_MESSAGE)
                val since = Timestamp.now().subDuration(hoursBack.hours)
                val filter = Filter()
                    .kind(kind)
                    .pubkey(userKeys.publicKey())
                    .since(since)
                    .limit(50uL)
                val events = client.fetchEvents(filter, 10.seconds)
                val list = events.toVec().map { event ->
                    event.author().toHex() to event.content()
                }
                Result.success(list)
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch DMs")
                Result.failure(e)
            }
        }
    }

    override fun close() {
        try {
            client.destroy()
            signer.destroy()
            userKeys.destroy()
            relays.clear()
        } catch (e: Exception) {
            Timber.w("Error closing Nostr client: %s", e.message)
        }
    }
}
