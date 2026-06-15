package com.ikoro.android.ui.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.ui.components.EmptyAnimations
import com.ikoro.android.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen() {
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calls") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Default.VideoCall, contentDescription = null) },
                text = { Text("New call") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice & Video Calls",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Powered by LiveKit on livekit.ugogbe.info",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            EmptyState(
                title = "No recent calls",
                subtitle = "Start an end-to-end encrypted voice or video call from a chat.",
                animationRes = EmptyAnimations.chat
            )
        }
    }

    if (showSheet) {
        StartCallSheet(onDismiss = { showSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartCallSheet(onDismiss: () -> Unit) {
    var room by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start a secure call",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LiveKit room on livekit.ugogbe.info",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Room name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // TODO: join LiveKit room
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = room.isNotBlank()
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.height(0.dp).weight(0.1f, fill = false))
                Text("Join call")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
