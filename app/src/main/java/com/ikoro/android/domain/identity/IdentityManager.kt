package com.ikoro.android.domain.identity

import com.ikoro.android.data.local.IdentityStore
import com.ikoro.android.data.model.Identity
import timber.log.Timber

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
            val mnemonic = Bip39Helper.generateMnemonic(wordCount = 24)
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
            val words = Bip39Helper.parseWords(mnemonic)
            require(Bip39Helper.isValidWordCount(words.size)) { "Mnemonic must be 12, 15, 18, 21 or 24 words" }
            Bip39Helper.validateMnemonic(words)
            val identity = deriveIdentity(words.joinToString(" "))
            store.saveIdentity(identity)
            Result.success(identity)
        } catch (e: Exception) {
            Timber.e(e, "Failed to restore identity")
            Result.failure(e)
        }
    }

    fun loadExistingIdentity(): Identity? = store.loadIdentity()

    fun deriveEvmKeyPair(): Result<org.web3j.crypto.ECKeyPair> {
        return try {
            val identity = store.loadIdentity() ?: return Result.failure(IllegalStateException("No identity"))
            val seed = Bip39Helper.mnemonicToSeed(identity.mnemonic)
            val pair = KeyDerivation.deriveEthereumKeyPair(seed)
            Result.success(pair)
        } catch (e: Exception) {
            Timber.e(e, "deriveEvmKeyPair failed")
            Result.failure(e)
        }
    }

    fun clearIdentity() {
        store.clearIdentity()
    }

    private fun deriveIdentity(mnemonic: String): Identity {
        val words = Bip39Helper.parseWords(mnemonic)
        val seed = Bip39Helper.validateMnemonic(words)
        val evmAddress = "0x" + KeyDerivation.deriveEvmAddress(seed)
        val rootstockAddress = "0x" + KeyDerivation.deriveRootstockAddress(seed)
        val nostr = KeyDerivation.deriveNostrKeyPair(seed)
        val npub = Bech32Nostr.encodeNpub(nostr.publicKey)
        val nsec = Bech32Nostr.encodeNsec(nostr.privateKey)
        val fingerprint = KeyDerivation.seedFingerprint(seed)
        val did = "did:ethr:$evmAddress"
        return Identity(
            mnemonic = mnemonic,
            seedFingerprint = fingerprint,
            nostrNpub = npub,
            nostrNsec = nsec,
            evmAddress = evmAddress,
            rootstockAddress = rootstockAddress,
            did = did,
        )
    }
}
