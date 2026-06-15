package com.ikoro.android.domain.identity

import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Minimal Bech32 encoder for Nostr npub1 / nsec1.
 * Nostr uses the HRP "npub" for public keys (32 bytes) and "nsec" for private keys (32 bytes).
 */
object Bech32Nostr {

    private const val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"

    fun encodeNpub(publicKey: ByteArray): String = encode("npub", publicKey)
    fun encodeNsec(privateKey: ByteArray): String = encode("nsec", privateKey)

    private fun encode(hrp: String, data: ByteArray): String {
        val data5 = convertBits(data, 8, 5, true)
        val polymod = polymod(hrpExpand(hrp) + data5 + List(6) { 0 })
        val checksum = (polymod xor 1).toChecksum()
        return hrp + "1" + data5.map { CHARSET[it] }.joinToString("") + checksum.map { CHARSET[it] }.joinToString("")
    }

    private fun Int.toChecksum(): List<Int> {
        return (0..5).map { i -> (this shr (5 * (5 - i))) and 31 }
    }

    private fun hrpExpand(hrp: String): List<Int> {
        return hrp.map { it.code shr 5 } + listOf(0) + hrp.map { it.code and 31 }
    }

    private fun polymod(values: List<Int>): Int {
        val generator = intArrayOf(0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3)
        var chk = 1
        for (v in values) {
            val b = chk shr 25
            chk = ((chk and 0x1ffffff) shl 5) xor v
            for (i in 0..4) {
                if (((b shr i) and 1) != 0) {
                    chk = chk xor generator[i]
                }
            }
        }
        return chk
    }

    private fun convertBits(data: ByteArray, fromBits: Int, toBits: Int, pad: Boolean): List<Int> {
        var acc = 0
        var bits = 0
        val ret = mutableListOf<Int>()
        val maxv = (1 shl toBits) - 1
        val maxAcc = (1 shl (fromBits + toBits - 1)) - 1
        for (value in data) {
            val b = value.toInt() and 0xff
            acc = ((acc shl fromBits) or b) and maxAcc
            bits += fromBits
            while (bits >= toBits) {
                bits -= toBits
                ret.add((acc shr bits) and maxv)
            }
        }
        if (pad) {
            if (bits > 0) {
                ret.add((acc shl (toBits - bits)) and maxv)
            }
        } else if (bits >= fromBits || ((acc shl (toBits - bits)) and maxv) != 0) {
            throw IllegalArgumentException("Could not convert bits")
        }
        return ret
    }
}
