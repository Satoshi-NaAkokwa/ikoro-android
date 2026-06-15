package com.ikoro.android.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ikoro.android.data.model.ChatContact
import com.ikoro.android.di.ServiceLocator

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val chatManager = remember(context) { ServiceLocator.chatManager(context) }

    // Pin Agabra support contact on first render
    LaunchedEffect(Unit) { chatManager.pinAgabra() }

    val selected = remember { mutableStateOf<ChatContact?>(null) }

    selected.value?.let { contact ->
        ChatDetailScreen(
            chatId = contact.id,
            title = contact.displayName,
            chatManager = chatManager
        )
        // User can return via bottom nav.
    } ?: run {
        ChatListScreen(
            chatManager = chatManager,
            onOpenChat = { contact: ChatContact -> selected.value = contact },
            onCreateGroup = { /* TODO */ }
        )
    }
}
