package com.ikoro.android.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Identity(
    val mnemonic: String,
    val nostrNpub: String = "",
    val nostrNsec: String = "",
    val evmAddress: String = "",
    val did: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
