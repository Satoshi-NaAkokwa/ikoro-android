package com.ikoro.android.domain.identity

import com.ikoro.android.data.local.IdentityStorage
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.wallet.WalletDerivation
import timber.log.Timber
import java.security.MessageDigest

class IdentityManager(
    private val store: IdentityStorage,
    private val walletDerivation: WalletDerivation
) {

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
            val mnemonic = generateMnemonic()
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
            // Validate by attempting derivation; TrustWalletCore will throw on invalid words
            walletDerivation.deriveAddresses(mnemonic)
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
        val derived = walletDerivation.deriveAddresses(mnemonic)
        val nostr = NostrDerivation.derive(mnemonic)
        val fingerprint = seedFingerprint(derived.seed)
        val did = "did:ethr:${derived.evmAddress}"
        return Identity(
            mnemonic = mnemonic,
            seedFingerprint = fingerprint,
            nostrNpub = nostr.npub,
            nostrNsec = nostr.nsec,
            evmAddress = derived.evmAddress,
            rootstockAddress = derived.rootstockAddress,
            did = did,
        )
    }

    private fun generateMnemonic(): String {
        // Use TrustWalletCore for proper 24-word BIP39 entropy
        return wallet.core.jni.HDWallet(256, "").mnemonic()
    }

    private fun seedFingerprint(seed: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(seed)
            .copyOfRange(0, 4)
            .joinToString("") { "%02x".format(it) }
    }
}
