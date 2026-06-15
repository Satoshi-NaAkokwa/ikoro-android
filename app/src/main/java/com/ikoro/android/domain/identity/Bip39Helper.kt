package com.ikoro.android.domain.identity

import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Locale

object Bip39Helper {

    @Throws(MnemonicException.MnemonicLengthException::class)
    fun generateMnemonic(wordCount: Int = 24): String {
        require(wordCount in listOf(12, 15, 18, 21, 24)) { "Invalid word count" }
        val entropyBits = wordCount * 11 - wordCount / 3
        val entropyBytes = entropyBits / 8
        val entropy = ByteArray(entropyBytes).apply { SecureRandom().nextBytes(this) }
        return MnemonicCode.INSTANCE.toMnemonic(entropy).joinToString(" ")
    }

    @Throws(MnemonicException::class)
    fun validateMnemonic(words: List<String>): ByteArray {
        MnemonicCode.INSTANCE.check(words)
        return mnemonicToSeed(words, "")
    }

    @Throws(MnemonicException::class)
    fun mnemonicToSeed(mnemonic: String): ByteArray {
        val words = parseWords(mnemonic)
        return validateMnemonic(words)
    }

    fun parseWords(mnemonic: String): List<String> {
        return mnemonic
            .trim()
            .lowercase(Locale.ROOT)
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    fun isValidWordCount(count: Int): Boolean = count in listOf(12, 15, 18, 21, 24)

    private fun mnemonicToSeed(words: List<String>, passphrase: String): ByteArray {
        val mnemonic = words.joinToString(" ")
        val salt = ("mnemonic" + passphrase).toByteArray(Charsets.UTF_8)
        val spec = PBEKeySpec(
            mnemonic.toCharArray(),
            salt,
            2048,
            512
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(spec).encoded
    }
}
