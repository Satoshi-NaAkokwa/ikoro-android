package com.ikoro.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "contacts")
data class ChatContact(
    @PrimaryKey val id: String,
    val displayName: String,
    val npub: String? = null,
    val serverUri: String? = null,
    val isGroup: Boolean = false,
    val isPinned: Boolean = false
)
