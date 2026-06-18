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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ikoro.android.R
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.ui.components.QrShareSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityScreen(identityManager: IdentityManager) {
    val context = LocalContext.current
    val identityState: State<Identity?> = produceState<Identity?>(null) {
        value = identityManager.loadExistingIdentity()
    }
    val identity = identityState.value
    val showSeedDialog = remember { mutableStateOf(false) }
    var qrValue by remember { mutableStateOf("") }
    var qrTitle by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    if (showSeedDialog.value && identity != null) {
        SeedRevealDialog(
            seed = identity.mnemonic,
            onDismiss = { showSeedDialog.value = false }
        )
    }

    if (qrValue.isNotBlank()) {
        QrShareSheet(
            title = qrTitle,
            value = qrValue,
            onDismiss = { qrValue = "" }
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
            if (identity == null) {
                Text("No identity found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    text = stringResource(R.string.your_identity),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "One BIP-39 seed controls your username, Nostr keys, DID, and every supported chain. Your EVM address is shared across Rootstock, Ethereum, Base, Polygon, Arbitrum, Optimism and BSC.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MasterIdentityCard(
                    fingerprint = identity.seedFingerprint,
                    evmAddress = identity.evmAddress,
                    onCopy = { copyToClipboard(context, identity.evmAddress) },
                    onQr = {
                        qrTitle = "EVM / Rootstock / DID address"
                        qrValue = identity.evmAddress
                    }
                )

                IdentityRow(
                    label = "Nostr public key",
                    value = identity.nostrNpub,
                    onCopy = { copyToClipboard(context, identity.nostrNpub) },
                    onQr = {
                        qrTitle = "Nostr npub"
                        qrValue = identity.nostrNpub
                    }
                )

                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(if (expanded) "Hide derived addresses" else "Show all derived addresses")
                }

                if (expanded) {
                    DerivedAddressesPanel(
                        evmAddress = identity.evmAddress,
                        rootstockAddress = identity.evmAddress,
                        did = identity.did,
                        nostrNpub = identity.nostrNpub,
                        onCopy = { copyToClipboard(context, it) }
                    )
                }

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
private fun MasterIdentityCard(
    fingerprint: String,
    evmAddress: String,
    onCopy: () -> Unit,
    onQr: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ikoro Identity",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = "Seed fingerprint: $fingerprint",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Primary EVM / DID / Rootstock address",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = evmAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onQr) {
                    Icon(Icons.Default.QrCode, contentDescription = "Show QR", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun DerivedAddressesPanel(
    evmAddress: String,
    rootstockAddress: String,
    did: String,
    nostrNpub: String,
    onCopy: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DerivedRow(label = "DID", value = did, derivation = "did:ethr:", onCopy = { onCopy(did) })
        DerivedRow(label = "EVM chains", value = evmAddress, derivation = "m/44'/60'/0'/0/0", onCopy = { onCopy(evmAddress) })
        DerivedRow(label = "Rootstock", value = rootstockAddress, derivation = "Same EVM address", onCopy = { onCopy(rootstockAddress) })
        DerivedRow(label = "Nostr", value = nostrNpub, derivation = "NIP-06 / BIP39 → nsec", onCopy = { onCopy(nostrNpub) })
        Text(
            text = "Bitcoin is derived independently under BIP-84/BIP-44 from the same seed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DerivedRow(
    label: String,
    value: String,
    derivation: String,
    onCopy: () -> Unit
) {
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
                Text(
                    text = derivation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }
    }
}

@Composable
private fun IdentityRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    onQr: (() -> Unit)? = null
) {
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
            Row {
                onQr?.let { qr ->
                    IconButton(onClick = qr) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Show QR"
                        )
                    }
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
