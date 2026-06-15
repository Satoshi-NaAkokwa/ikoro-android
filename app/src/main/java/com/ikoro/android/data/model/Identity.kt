package com.ikoro.android.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Identity(
    val mnemonic: String,
    val seedFingerprint: String,
    val nostrNpub: String,
    val nostrNsec: String,
    val evmAddress: String,
    val rootstockAddress: String,
    val did: String,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
