package com.ikoro.android.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.ikoro.android.data.model.ChatContact
import com.ikoro.android.data.remote.LiveKitCallManager
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.ui.calls.RoomActivity
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val chatManager = remember(context) { ServiceLocator.chatManager(context) }
    val scope = rememberCoroutineScope()
    val callManager = remember { LiveKitCallManager(context) }

    // Pin Agabra support contact on first render
    LaunchedEffect(Unit) { chatManager.pinAgabra() }

    val selected = remember { mutableStateOf<ChatContact?>(null) }

    selected.value?.let { contact ->
        ChatDetailScreen(
            chatId = contact.id,
            title = contact.displayName,
            contactNpub = contact.npub,
            chatManager = chatManager,
            onStartCall = {
                val roomName = "ikoro-${contact.id.take(16)}"
                scope.launch {
                    callManager.fetchToken(roomName, "ikoro-user").onSuccess { token ->
                        RoomActivity.start(context, token, roomName)
                    }.onFailure {
                        // Caller can surface the error via a snackbar if desired
                    }
                }
            }
        )
    } ?: run {
        ChatListScreen(
            chatManager = chatManager,
            onOpenChat = { contact: ChatContact -> selected.value = contact },
            onCreateGroup = {
                // Handled inside ChatListScreen via its own dialog
            }
        )
    }
}
