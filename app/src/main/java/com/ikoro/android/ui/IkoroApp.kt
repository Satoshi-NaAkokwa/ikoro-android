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
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.ui.main.MainShell
import com.ikoro.android.ui.onboarding.OnboardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun IkoroApp(identityManager: IdentityManager) {
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
                    ServiceLocator.walletManager(identityManager).initialize()
                } catch (e: Exception) {
                    Timber.e(e, "WalletManager init failed")
                }
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
                identityManager = identityManager,
                onResetIdentity = {
                    identityManager.clearIdentity()
                    hasIdentity = false
                }
            )
        }
    }
}
