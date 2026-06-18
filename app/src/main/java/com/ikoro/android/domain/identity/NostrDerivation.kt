package com.ikoro.android.domain.identity

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.asn1.x9.X9ECPoint
import java.math.BigInteger
import java.security.Security
import java.security.MessageDigest

object NostrDerivation {

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun derive(mnemonic: String): NostrKeys {
        val seed = mnemonicToSeed(mnemonic)
        val master = bip32FromSeed(seed)
        val derived = master.deriveHardened(44).deriveHardened(1237).deriveHardened(0).derive(0).derive(0)
        val privKey = derived.key.toByteArray().let { if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else it.copyOf(32) }
        val pubKey = publicKeyFromPrivateKey(privKey)
        return NostrKeys(
            npub = encodeBech32("npub", pubKey),
            nsec = encodeBech32("nsec", privKey)
        )
    }

    data class NostrKeys(val npub: String, val nsec: String)

    internal fun mnemonicToEntropy(mnemonic: String): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(mnemonic.toByteArray())
    }

    private fun mnemonicToSeed(mnemonic: String): ByteArray {
        val salt = "mnemonic".toByteArray()
        val gen = PKCS5S2ParametersGenerator(SHA256Digest())
        gen.init(mnemonic.toByteArray(), salt, 2048)
        return (gen.generateDerivedParameters(512) as KeyParameter).key
    }

    private fun publicKeyFromPrivateKey(privateKey: ByteArray): ByteArray {
        val curve = SECNamedCurves.getByName("secp256k1")
        val domainParams = X9ECParameters(curve.curve, X9ECPoint(curve.g, true), curve.n, curve.h)
        val g = domainParams.g
        val d = BigInteger(1, privateKey)
        val q = g.multiply(d)
        return q.getEncoded(true)
    }

    private data class Bip32Node(val key: BigInteger, val chainCode: ByteArray) {
        fun deriveHardened(index: Int): Bip32Node = derive(index or 0x80000000.toInt())
        fun derive(index: Int): Bip32Node {
            val data = ByteArray(37)
            val keyBytes = key.toByteArray().let { if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else it }
            keyBytes.copyInto(data, 1, maxOf(0, keyBytes.size - 32))
            data[0] = 0x02
            data[33] = (index ushr 24).toByte()
            data[34] = (index ushr 16).toByte()
            data[35] = (index ushr 8).toByte()
            data[36] = index.toByte()
            val hmac = javax.crypto.Mac.getInstance("HmacSHA512").apply {
                init(javax.crypto.spec.SecretKeySpec(chainCode, "HmacSHA512"))
            }.doFinal(data)
            val childKey = BigInteger(1, hmac.copyOfRange(0, 32))
            val childChain = hmac.copyOfRange(32, 64)
            return Bip32Node(childKey, childChain)
        }
    }

    private fun bip32FromSeed(seed: ByteArray): Bip32Node {
        val hmac = javax.crypto.Mac.getInstance("HmacSHA512").apply {
            init(javax.crypto.spec.SecretKeySpec("Bitcoin seed".toByteArray(), "HmacSHA512"))
        }.doFinal(seed)
        return Bip32Node(BigInteger(1, hmac.copyOfRange(0, 32)), hmac.copyOfRange(32, 64))
    }

    // --- Bech32 encoder ---
    private val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"
    private val GENERATORS = intArrayOf(0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3)

    private fun polymod(values: IntArray): Int {
        var chk = 1
        for (v in values) {
            val b = chk ushr 25
            chk = ((chk and 0x1ffffff) shl 5) xor v
            for (i in 0 until 5) {
                if (((b ushr i) and 1) == 1) {
                    chk = chk xor GENERATORS[i]
                }
            }
        }
        return chk
    }

    private fun hrpExpand(hrp: String): IntArray {
        val p1 = IntArray(hrp.length) { hrp[it].code ushr 5 }
        val p2 = IntArray(hrp.length) { hrp[it].code and 31 }
        return p1 + intArrayOf(0) + p2
    }

    private fun convertBits(data: ByteArray, fromBits: Int, toBits: Int, pad: Boolean): IntArray {
        var acc = 0
        var bits = 0
        val ret = mutableListOf<Int>()
        val maxv = (1 shl toBits) - 1
        val maxAcc = (1 shl (fromBits + toBits - 1)) - 1
        for (value in data) {
            acc = ((acc shl fromBits) or (value.toInt() and 0xff)) and maxAcc
            bits += fromBits
            while (bits >= toBits) {
                bits -= toBits
                ret.add((acc ushr bits) and maxv)
            }
        }
        if (pad) {
            if (bits > 0) ret.add((acc shl (toBits - bits)) and maxv)
        } else if (bits >= fromBits || ((acc shl (toBits - bits)) and maxv) != 0) {
            throw IllegalArgumentException("Invalid bit conversion")
        }
        return ret.toIntArray()
    }

    private fun encodeBech32(hrp: String, data: ByteArray): String {
        val data5 = convertBits(data, 8, 5, true)
        val values = hrpExpand(hrp) + data5
        val polymod = polymod(values + intArrayOf(0, 0, 0, 0, 0, 0)) xor 1
        val checksum = IntArray(6) { (polymod ushr (5 * (5 - it))) and 31 }
        val dataPart = data5.joinToString("") { CHARSET[it].toString() }
        val checkPart = checksum.joinToString("") { CHARSET[it].toString() }
        return "${hrp}1${dataPart}${checkPart}"
    }
}
