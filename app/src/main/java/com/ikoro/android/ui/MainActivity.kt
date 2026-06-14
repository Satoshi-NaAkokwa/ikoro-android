package com.ikoro.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.ui.theme.IkoroTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        renderApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (missingPermissions().isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            renderApp()
        }
    }

    private fun renderApp() {
        setContent {
            IkoroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IkoroApp(identityManager = ServiceLocator.identityManager)
                }
            }
        }
    }

    private fun missingPermissions(): List<String> {
        return requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
    }
}
