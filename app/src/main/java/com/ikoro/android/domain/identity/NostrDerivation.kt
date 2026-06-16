package com.ikoro.android.domain.identity

import rust.nostr.sdk.Keys
import rust.nostr.sdk.SecretKey
import java.security.MessageDigest

object NostrDerivation {

    fun derive(mnemonic: String): NostrKeys {
        val entropy = mnemonicToEntropy(mnemonic)
        val hex = entropy.toHex()
        val secretKey = SecretKey.parse(hex)
        val keys = Keys(secretKey = secretKey)
        return NostrKeys(
            npub = keys.publicKey().toBech32(),
            nsec = secretKey.toBech32()
        )
    }

    data class NostrKeys(val npub: String, val nsec: String)

    private fun mnemonicToEntropy(mnemonic: String): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(mnemonic.toByteArray())
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
