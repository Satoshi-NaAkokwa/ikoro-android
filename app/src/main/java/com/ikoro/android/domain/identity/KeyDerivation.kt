package com.ikoro.android.domain.identity

import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import java.math.BigInteger
import java.security.MessageDigest

object KeyDerivation {

    // EVM (BIP-44): m/44'/60'/0'/0/0
    fun deriveEvmAddress(seed: ByteArray): String {
        val key = derivePath(seed, listOf(
            hard(44), hard(60), hard(0), normal(0), normal(0)
        ))
        val ecPair = ECKeyPair.create(key.privKeyBytes)
        return Keys.getAddress(ecPair)
    }

    // Rootstock uses same path with did:pkh
    fun deriveRootstockAddress(seed: ByteArray): String {
        return deriveEvmAddress(seed)
    }

    // Nostr (BIP-44 style): m/44'/1237'/0'/0/0
    fun deriveNostrKeyPair(seed: ByteArray): NostrKeyPair {
        val key = derivePath(seed, listOf(
            hard(44), hard(1237), hard(0), normal(0), normal(0)
        ))
        val priv = key.privKeyBytes.copyOf(32)
        val pub = publicKeyFromPrivate(key.privKey)
        val xOnlyPub = pub.copyOfRange(1, 33)
        return NostrKeyPair(privateKey = priv, publicKey = xOnlyPub)
    }

    private fun derivePath(seed: ByteArray, path: List<ChildNumber>): DeterministicKey {
        var key: DeterministicKey = HDKeyDerivation.createMasterPrivateKey(seed)
        for (child in path) {
            key = HDKeyDerivation.deriveChildKey(key, child)
        }
        return key
    }

    private fun hard(index: Int) = ChildNumber(index, true)
    private fun normal(index: Int) = ChildNumber(index, false)

    data class NostrKeyPair(
        val privateKey: ByteArray,
        val publicKey: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NostrKeyPair) return false
            return privateKey.contentEquals(other.privateKey) && publicKey.contentEquals(other.publicKey)
        }

        override fun hashCode(): Int {
            var result = privateKey.contentHashCode()
            result = 31 * result + publicKey.contentHashCode()
            return result
        }
    }

    private fun publicKeyFromPrivate(priv: BigInteger): ByteArray {
        val point = org.web3j.crypto.Sign.publicPointFromPrivate(priv)
        return point.getEncoded(true)
    }

    fun seedFingerprint(seed: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(seed)
            .copyOfRange(0, 4)
            .joinToString("") { "%02x".format(it) }
    }
}
