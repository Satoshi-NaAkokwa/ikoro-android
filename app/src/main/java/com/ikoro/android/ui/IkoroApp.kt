package com.ikoro.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.ui.onboarding.OnboardingScreen
import com.ikoro.android.ui.main.MainShell

@Composable
fun IkoroApp(identityManager: IdentityManager) {
    var hasIdentity by remember { mutableStateOf(identityManager.hasIdentity()) }

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
