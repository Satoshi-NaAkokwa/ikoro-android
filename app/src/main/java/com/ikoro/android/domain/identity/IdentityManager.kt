package com.ikoro.android.domain.identity

import com.ikoro.android.data.local.IdentityStore
import com.ikoro.android.data.model.Identity
import rust.nostr.sdk.Keys
import rust.nostr.sdk.SecretKey
import timber.log.Timber
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.security.MessageDigest

class IdentityManager(private val store: IdentityStore) {

    fun hasValidIdentity(): Boolean {
        if (!store.hasIdentity) return false
        val id = store.loadIdentity() ?: return false
        return id.mnemonic.isNotBlank()
                && id.nostrNpub.isNotBlank()
                && id.evmAddress.isNotBlank()
                && id.did.isNotBlank()
    }

    fun resetIfCorrupt() {
        if (!hasValidIdentity()) {
            store.clearIdentity()
        }
    }

    fun createIdentity(): Result<Identity> {
        return try {
            val mnemonic = HDWallet(256, "").mnemonic()
            val identity = deriveIdentity(mnemonic)
            store.saveIdentity(identity)
            Result.success(identity)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create identity")
            Result.failure(e)
        }
    }

    fun restoreIdentity(mnemonic: String): Result<Identity> {
        return try {
            val words = mnemonic.trim().split(Regex("\\s+"))
            require(words.size in listOf(12, 15, 18, 21, 24)) { "Mnemonic must be 12, 15, 18, 21 or 24 words" }
            HDWallet(mnemonic, "")
            val identity = deriveIdentity(mnemonic)
            store.saveIdentity(identity)
            Result.success(identity)
        } catch (e: Exception) {
            Timber.e(e, "Failed to restore identity")
            Result.failure(e)
        }
    }

    fun loadExistingIdentity(): Identity? = store.loadIdentity()

    fun loadMnemonic(): String? = store.loadIdentity()?.mnemonic

    fun loadNsec(): String? = store.loadIdentity()?.nostrNsec

    fun clearIdentity() {
        store.clearIdentity()
    }

    private fun deriveIdentity(mnemonic: String): Identity {
        val wallet = HDWallet(mnemonic, "")
        val evmAddress = wallet.getAddressForCoin(CoinType.ETHEREUM)
        val rootstockAddress = wallet.getAddressForCoin(CoinType.ROOTSTOCK)
        val nostr = NostrDerivation.derive(mnemonic)
        val fingerprint = seedFingerprint(wallet.seed())
        val did = "did:ethr:$evmAddress"
        return Identity(
            mnemonic = mnemonic,
            seedFingerprint = fingerprint,
            nostrNpub = nostr.npub,
            nostrNsec = nostr.nsec,
            evmAddress = evmAddress,
            rootstockAddress = rootstockAddress,
            did = did,
        )
    }

    private fun seedFingerprint(seed: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(seed)
            .copyOfRange(0, 4)
            .joinToString("") { "%02x".format(it) }
    }
}
