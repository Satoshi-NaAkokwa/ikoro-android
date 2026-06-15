package com.ikoro.android.ui.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.ui.components.EmptyAnimations
import com.ikoro.android.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Calls") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* start new call */ },
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
}
