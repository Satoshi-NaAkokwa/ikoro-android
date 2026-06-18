package com.ikoro.android.data.model

data class DerivedAddresses(
    val evmAddress: String,
    val rootstockAddress: String,
    val seed: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DerivedAddresses) return false
        return evmAddress == other.evmAddress && rootstockAddress == other.rootstockAddress && seed.contentEquals(other.seed)
    }

    override fun hashCode(): Int {
        var result = evmAddress.hashCode()
        result = 31 * result + rootstockAddress.hashCode()
        result = 31 * result + seed.contentHashCode()
        return result
    }
}
