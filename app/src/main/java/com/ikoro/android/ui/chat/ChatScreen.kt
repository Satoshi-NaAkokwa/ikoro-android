package com.ikoro.android.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ikoro.android.di.ServiceLocator

@Composable
fun ChatScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val chatManager = remember(context) { ServiceLocator.chatManager(context) }
    // Simple 1:1 chat demo with a default chat id
    ChatDetailScreen(chatId = "demo", chatManager = chatManager)
}
