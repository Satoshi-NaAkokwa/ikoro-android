package com.ikoro.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ikoro.android.data.model.ChatContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isPinned DESC, displayName ASC")
    fun allContacts(): Flow<List<ChatContact>>

    @Query("SELECT * FROM contacts ORDER BY isPinned DESC, displayName ASC")
    suspend fun allContactsOnce(): List<ChatContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ChatContact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun delete(id: String)
}
