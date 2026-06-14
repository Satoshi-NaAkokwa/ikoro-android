package com.ikoro.android.domain.identity

import com.ikoro.android.data.local.IdentityStore
import com.ikoro.android.data.model.Identity
import timber.log.Timber
import java.security.SecureRandom

class IdentityManager(private val store: IdentityStore) {

    private val wordList: List<String> by lazy { loadWordList() }

    fun hasIdentity(): Boolean = store.hasIdentity

    fun createIdentity(): Result<Identity> {
        return try {
            val entropy = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val words = entropyToMnemonic(entropy)
            val identity = deriveIdentity(words)
            store.saveIdentity(identity)
            Result.success(identity)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create identity")
            Result.failure(e)
        }
    }

    fun restoreIdentity(mnemonic: String): Result<Identity> {
        return try {
            val words = mnemonic.trim().lowercase().split(Regex("\\s+"))
            require(words.size in listOf(12, 15, 18, 21, 24)) { "Invalid mnemonic length" }
            val identity = deriveIdentity(words.joinToString(" "))
            store.saveIdentity(identity)
            Result.success(identity)
        } catch (e: Exception) {
            Timber.e(e, "Failed to restore identity")
            Result.failure(e)
        }
    }

    fun loadExistingIdentity(): Identity? = store.loadIdentity()

    private fun deriveIdentity(mnemonic: String): Identity {
        val seedHash = IdentityStore.sha256(mnemonic)
        val npub = "npub1" + seedHash.take(32)
        val evm = "0x" + seedHash.take(40)
        val did = "did:pkh:eip155:30:$evm"
        return Identity(
            mnemonic = mnemonic,
            nostrNpub = npub,
            evmAddress = evm,
            did = did
        )
    }

    private fun entropyToMnemonic(entropy: ByteArray): String {
        // Simplified BIP-39: pick random words from the English wordlist.
        // For production this must implement full BIP-39 checksum.
        val indices = entropy.map { it.toInt() and 0xFF }.take(12)
        return indices.map { wordList[it % wordList.size] }.joinToString(" ")
    }

    private fun loadWordList(): List<String> {
        return listOf(
            "abandon","ability","able","about","above","absent","absorb","abstract","absurd","abuse",
            "access","accident","account","accuse","achieve","acid","acoustic","acquire","across","act",
            "action","actor","actress","actual","adapt","add","addict","address","adjust","admit",
            "adult","advance","advice","aerobic","affair","afford","afraid","african","after","again"
        )
    }
}
