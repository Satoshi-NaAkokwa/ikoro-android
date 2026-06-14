package com.ikoro.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ikoro.android.data.model.ChatContact
import com.ikoro.android.data.model.ChatMessage

@Database(entities = [ChatMessage::class, ChatContact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
}
