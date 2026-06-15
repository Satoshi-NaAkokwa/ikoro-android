package com.ikoro.android.domain.chat

import com.ikoro.android.data.local.ContactDao
import com.ikoro.android.data.local.MessageDao
import com.ikoro.android.data.model.ChatContact
import com.ikoro.android.data.model.ChatMessage
import com.ikoro.android.data.remote.SimplexBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

private const val AGABRA_WELCOME = "Hi — I'm Agbara, your Ikoro support contact. This channel is private and tied only to you."

class ChatManager(
    private val bridge: SimplexBridge,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _contacts = MutableStateFlow<List<ChatContact>>(emptyList())
    val contacts: StateFlow<List<ChatContact>> = _contacts

    private val _events = MutableStateFlow("")
    val events: StateFlow<String> = _events

    init {
        bridge.setListener(object : com.ikoro.android.data.remote.SimplexEventListener {
            override fun onEvent(event: String) {
                _events.value = event
                if (event.startsWith("invitation_accepted:")) {
                    val connReq = event.removePrefix("invitation_accepted:")
                    scope.launch {
                        addContact("New contact", "conn:$connReq")
                    }
                }
            }
        })
        scope.launch {
            contactDao.allContacts().collect { _contacts.value = it }
        }
    }

    fun initialize(serverUri: String, profileName: String) {
        bridge.connect(serverUri)
        bridge.createProfile(profileName)
        bridge.startChat()
    }

    suspend fun addContact(name: String, npub: String) {
        val contact = ChatContact(
            id = UUID.randomUUID().toString(),
            displayName = name,
            npub = npub,
            isPinned = npub == com.ikoro.android.BuildConfig.AGABRA_NPUB
        )
        contactDao.insert(contact)
    }

    suspend fun pinAgabra() {
        val agabraNpub = com.ikoro.android.BuildConfig.AGABRA_NPUB
        val existing = contactDao.allContactsOnce().any { it.npub == agabraNpub }
        if (!existing) {
            val contact = ChatContact(
                id = UUID.randomUUID().toString(),
                displayName = "Agbara (Ikoro Support)",
                npub = agabraNpub,
                isPinned = true
            )
            contactDao.insert(contact)
            sendMessage(contact.id, AGABRA_WELCOME)
        }
    }

    suspend fun createGroup(name: String) {
        bridge.createGroup(name)
        val group = ChatContact(
            id = UUID.randomUUID().toString(),
            displayName = name,
            isGroup = true
        )
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
