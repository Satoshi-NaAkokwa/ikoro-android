package com.ikoro.android.data.model

data class Asset(
    val id: String,
    val chainName: String,
    val symbol: String,
    val address: String,
    val balance: String = "0",
    val fiatValue: String = "\u00240.00",
    val accentColor: Long = 0xFF000000,
    val isPrimary: Boolean = false
)
