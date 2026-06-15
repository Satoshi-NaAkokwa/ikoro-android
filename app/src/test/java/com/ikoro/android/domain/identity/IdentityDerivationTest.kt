package com.ikoro.android.domain.identity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentityDerivationTest {

    @Test
    fun `bip39 mnemonic has 12 words and validates`() {
        val mnemonic = Bip39Helper.generateMnemonic(12)
        val words = Bip39Helper.parseWords(mnemonic)
        assertEquals(12, words.size)
        Bip39Helper.validateMnemonic(words) // should not throw
    }

    @Test
    fun `evm address starts with 0x and is 42 chars`() {
        val seed = Bip39Helper.mnemonicToSeed("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about")
        val addr = "0x" + KeyDerivation.deriveEvmAddress(seed)
        assertTrue(addr.startsWith("0x"))
        assertEquals(42, addr.length)
    }

    @Test
    fun `same mnemonic derives same nostr bech32 keys`() {
        val seed = Bip39Helper.mnemonicToSeed("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about")
        val keys1 = KeyDerivation.deriveNostrKeyPair(seed)
        val keys2 = KeyDerivation.deriveNostrKeyPair(seed)
        assertEquals(keys1, keys2)
        val npub = Bech32Nostr.encodeNpub(keys1.publicKey)
        val nsec = Bech32Nostr.encodeNsec(keys1.privateKey)
        assertTrue(npub.startsWith("npub1"))
        assertTrue(nsec.startsWith("nsec1"))
    }

    @Test
    fun `seed fingerprint is 8 hex chars`() {
        val seed = Bip39Helper.mnemonicToSeed("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about")
        val fp = KeyDerivation.seedFingerprint(seed)
        assertEquals(8, fp.length)
        assertTrue(fp.all { it.isDigit() || it in 'a'..'f' })
    }
}
