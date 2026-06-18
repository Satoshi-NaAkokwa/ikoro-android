package com.ikoro.android.ui.calls

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ikoro.android.BuildConfig
import com.ikoro.android.ui.theme.IkoroTheme
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalAudioTrackOptions
import io.livekit.android.room.track.LocalVideoTrackOptions
import kotlinx.coroutines.launch
import timber.log.Timber

class RoomActivity : ComponentActivity() {

    private var room: Room? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: ""
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: "ikoro-room"

        if (token.isBlank()) {
            Toast.makeText(this, "Missing LiveKit token", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }

        val roomInstance = LiveKit.create(
            this,
            RoomOptions(
                adaptiveStream = false,
                dynacast = false,
                audioTrackCaptureDefaults = LocalAudioTrackOptions(),
                videoTrackCaptureDefaults = LocalVideoTrackOptions()
            )
        )
        room = roomInstance

        setContent {
            IkoroTheme {
                RoomScreen(
                    roomName = roomName,
                    onDisconnect = { finish() }
                )
            }
        }

        lifecycleScope.launch {
            try {
                roomInstance.connect(BuildConfig.LIVEKIT_URL, token, ConnectOptions())
                roomInstance.localParticipant.setMicrophoneEnabled(true)
                roomInstance.localParticipant.setCameraEnabled(false)
            } catch (e: Exception) {
                Timber.e(e, "LiveKit connect failed")
                Toast.makeText(this@RoomActivity, "Call connect failed: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "Camera and microphone permissions are required for calls", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        room?.disconnect()
        room?.release()
    }

    companion object {
        private const val EXTRA_TOKEN = "token"
        private const val EXTRA_ROOM_NAME = "room_name"
        private const val PERMISSION_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        fun start(context: Context, token: String, roomName: String) {
            val intent = Intent(context, RoomActivity::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_ROOM_NAME, roomName)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
private fun RoomScreen(
    roomName: String,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    var micEnabled by remember { mutableStateOf(true) }
    var camEnabled by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Joining $roomName", style = MaterialTheme.typography.headlineSmall)
                Text("Audio enabled · Camera off by default", style = MaterialTheme.typography.bodyMedium)
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))

                androidx.compose.foundation.layout.Row(modifier = Modifier.padding(top = 24.dp)) {
                    IconButton(onClick = { micEnabled = !micEnabled }) {
                        Icon(
                            if (micEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Toggle mic"
                        )
                    }
                    IconButton(onClick = { camEnabled = !camEnabled }) {
                        Icon(
                            if (camEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Toggle camera"
                        )
                    }
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End call")
                    }
                }
            }
        }
    }
}
