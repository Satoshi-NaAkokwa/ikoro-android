package com.ikoro.android.ui.identity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ikoro.android.R
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.identity.IdentityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityScreen(identityManager: IdentityManager) {
    val context = LocalContext.current
    val identityState: State<Identity?> = produceState<Identity?>(null) {
        value = identityManager.loadExistingIdentity()
    }
    val identity = identityState.value
    val showSeedDialog = remember { mutableStateOf(false) }

    if (showSeedDialog.value && identity != null) {
        SeedRevealDialog(
            seed = identity.mnemonic,
            onDismiss = { showSeedDialog.value = false }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.identity)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.your_identity),
                style = MaterialTheme.typography.headlineSmall
            )
            if (identity == null) {
                Text("No identity found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    text = "One seed. One identity. One backup for Nostr, DID, and every EVM chain.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IdentityRow(
                    label = stringResource(R.string.did_label),
                    value = identity.did,
                    onCopy = { copyToClipboard(context, identity.did) }
                )
                IdentityRow(
                    label = stringResource(R.string.nostr_label),
                    value = identity.nostrNpub,
                    onCopy = { copyToClipboard(context, identity.nostrNpub) }
                )
                IdentityRow(
                    label = stringResource(R.string.evm_label),
                    value = identity.evmAddress,
                    onCopy = { copyToClipboard(context, identity.evmAddress) }
                )
                IdentityRow(
                    label = stringResource(R.string.rootstock_label),
                    value = identity.rootstockAddress,
                    onCopy = { copyToClipboard(context, identity.rootstockAddress) }
                )
                IdentityRow(
                    label = stringResource(R.string.seed_fingerprint_label),
                    value = identity.seedFingerprint,
                    onCopy = { copyToClipboard(context, identity.seedFingerprint) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        requestAuthentication(context) { showSeedDialog.value = true }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text("Reveal recovery seed")
                }
            }
        }
    }
}

@Composable
private fun SeedRevealDialog(seed: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recovery seed phrase") },
        text = {
            Column {
                Text(
                    "Anyone with these words controls your identity, wallet and chat history. Do not share or screenshot.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = seed,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                copyToClipboard(context, seed)
                onDismiss()
            }) {
                Text("Copy and close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
private fun IdentityRow(label: String, value: String, onCopy: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy)
                )
            }
        }
    }
}

private fun requestAuthentication(context: Context, onSuccess: () -> Unit) {
    val activity = context as? FragmentActivity ?: run {
        Toast.makeText(context, "Biometric auth requires FragmentActivity", Toast.LENGTH_SHORT).show()
        onSuccess()
        return
    }
    val biometricManager = BiometricManager.from(context)
    val canAuth = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
        Toast.makeText(context, "Biometric/PIN not available", Toast.LENGTH_SHORT).show()
        onSuccess()
        return
    }
    val executor = ContextCompat.getMainExecutor(context)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(context, "Auth error: $errString", Toast.LENGTH_SHORT).show()
            }
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Reveal recovery seed")
        .setSubtitle("Authenticate to view your seed phrase")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    prompt.authenticate(info)
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Ikoro identity", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
