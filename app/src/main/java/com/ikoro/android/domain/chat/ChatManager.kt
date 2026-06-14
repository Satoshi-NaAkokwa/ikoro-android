package com.ikoro.android.domain.chat

import com.ikoro.android.data.local.ContactDao
import com.ikoro.android.data.local.MessageDao
import com.ikoro.android.data.model.ChatContact
import com.ikoro.android.data.model.ChatMessage
import com.ikoro.android.data.remote.SimplexBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.UUID

class ChatManager(
    private val bridge: SimplexBridge,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao
) {
    private val _contacts = MutableStateFlow<List<ChatContact>>(emptyList())
    val contacts: StateFlow<List<ChatContact>> = _contacts

    fun initialize(serverUri: String, profileName: String) {
        bridge.connect(serverUri)
        bridge.createProfile(profileName)
        bridge.startChat()
    }

    suspend fun addContact(name: String, npub: String) {
        val contact = ChatContact(id = UUID.randomUUID().toString(), displayName = name, npub = npub)
        contactDao.insert(contact)
    }

    suspend fun createGroup(name: String) {
        bridge.createGroup(name)
        val group = ChatContact(id = UUID.randomUUID().toString(), displayName = name, isGroup = true)
        contactDao.insert(group)
    }

    suspend fun sendMessage(chatId: String, text: String) {
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            sender = "me",
            text = text,
            timestamp = System.currentTimeMillis(),
            isIncoming = false
        )
        messageDao.insert(message)
        bridge.sendMessage(chatId, text)
    }

    suspend fun receiveMessage(chatId: String, sender: String, text: String) {
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            sender = sender,
            text = text,
            timestamp = System.currentTimeMillis(),
            isIncoming = true
        )
        messageDao.insert(message)
    }

    fun messages(chatId: String): Flow<List<ChatMessage>> = messageDao.messagesForChat(chatId)
    fun allContacts(): Flow<List<ChatContact>> = contactDao.allContacts()

    fun shutdown() {
        bridge.disconnect()
    }
}
