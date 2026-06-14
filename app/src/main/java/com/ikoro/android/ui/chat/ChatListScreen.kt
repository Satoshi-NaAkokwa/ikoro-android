package com.ikoro.android.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.data.model.ChatContact
import com.ikoro.android.domain.chat.ChatManager

@Composable
fun ChatListScreen(chatManager: ChatManager, onOpenChat: (String) -> Unit, onCreateGroup: () -> Unit) {
    val contacts by chatManager.allContacts().collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroup) {
                Icon(Icons.Default.Add, contentDescription = "Create group")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(contacts, key = { it.id }) { contact ->
                ContactRow(contact = contact, onClick = { onOpenChat(contact.id) })
            }
        }
    }
}

@Composable
private fun ContactRow(contact: ChatContact, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = contact.displayName, style = MaterialTheme.typography.titleMedium)
        Text(
            text = if (contact.isGroup) "Group" else (contact.npub ?: "Direct"),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
