package com.ikoro.android.domain.identity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentityDerivationTest {

    private val testMnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    @Test
    fun `nostr derivation is deterministic and bech32`() {
        val keys1 = NostrDerivation.derive(testMnemonic)
        val keys2 = NostrDerivation.derive(testMnemonic)
        assertEquals(keys1, keys2)
        assertTrue(keys1.npub.startsWith("npub1"))
        assertTrue(keys1.nsec.startsWith("nsec1"))
    }

    @Test
    fun `seed fingerprint is 8 hex chars`() {
        val entropy = NostrDerivation.mnemonicToEntropy(testMnemonic)
        val fp = entropy.toHex().substring(0, 8)
        assertEquals(8, fp.length)
        assertTrue(fp.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun `restore identity produces valid identity fields`() {
        val manager = IdentityManager(StubIdentityStorage(), StubWalletDerivation())
        val result = manager.restoreIdentity(testMnemonic)
        assertTrue("restore failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val identity = result.getOrThrow()
        assertNotNull(identity)
        assertTrue(identity.evmAddress.startsWith("0x"))
        assertTrue(identity.nostrNpub.startsWith("npub1"))
        assertTrue(identity.nostrNsec.startsWith("nsec1"))
        assertEquals(8, identity.seedFingerprint.length)
    }

    @Test
    fun `invalid mnemonic word count is rejected`() {
        val manager = IdentityManager(StubIdentityStorage(), StubWalletDerivation())
        val result = manager.restoreIdentity("one two three")
        assertTrue(result.isFailure)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
