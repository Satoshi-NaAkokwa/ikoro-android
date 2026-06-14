package com.ikoro.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    val id: String,
    val issuer: String,
    val type: String,
    val payload: String,
    val issuedAt: Long
)
