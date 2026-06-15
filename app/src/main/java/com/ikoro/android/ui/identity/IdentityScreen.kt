package com.ikoro.android.ui.identity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                    onClick = { copyToClipboard(context, identity.mnemonic) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copy recovery seed (keep secret)")
                }
            }
        }
    }
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

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Ikoro identity", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
