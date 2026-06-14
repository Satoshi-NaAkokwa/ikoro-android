package com.ikoro.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val chatId: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val isIncoming: Boolean,
    val attachmentUri: String? = null
)
