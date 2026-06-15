package com.ikoro.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.ui.onboarding.OnboardingScreen
import com.ikoro.android.ui.main.MainShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun IkoroApp(identityManager: IdentityManager) {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    var hasIdentity by remember { mutableStateOf(false) }

    if (!ready) {
        identityManager.resetIfCorrupt()
        hasIdentity = identityManager.hasValidIdentity()
        ready = true
    }

    LaunchedEffect(hasIdentity) {
        if (hasIdentity) {
            withContext(Dispatchers.IO) {
                try {
                    ServiceLocator.walletManager().initialize()
                } catch (e: Exception) {
                    Timber.e(e, "WalletManager init failed")
                }
                ServiceLocator.chatManager(context).initialize(
                    serverUri = com.ikoro.android.BuildConfig.SMP_SERVER_URI,
                    profileName = "Ikoro user"
                )
            }
        }
    }

    if (!hasIdentity) {
        OnboardingScreen(
            identityManager = identityManager,
            onComplete = { hasIdentity = true }
        )
    } else {
        Scaffold { padding ->
            MainShell(
                modifier = Modifier.padding(padding),
                identityManager = identityManager
            )
        }
    }
}
