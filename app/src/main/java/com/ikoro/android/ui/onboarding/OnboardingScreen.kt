package com.ikoro.android.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ikoro.android.R
import com.ikoro.android.domain.identity.IdentityManager

@Composable
fun OnboardingScreen(
    identityManager: IdentityManager,
    onComplete: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var showRestore by remember { mutableStateOf(false) }

    when {
        showCreate -> CreateIdentityScreen(
            identityManager = identityManager,
            onDone = { showCreate = false; onComplete() },
            onBack = { showCreate = false }
        )
        showRestore -> RestoreIdentityScreen(
            identityManager = identityManager,
            onDone = { showRestore = false; onComplete() },
            onBack = { showRestore = false }
        )
        else -> WelcomeScreen(
            onCreate = { showCreate = true },
            onRestore = { showRestore = true }
        )
    }
}

@Composable
private fun WelcomeScreen(onCreate: () -> Unit, onRestore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create_identity))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRestore,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.restore_identity))
        }
    }
}
