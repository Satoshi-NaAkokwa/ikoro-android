package com.ikoro.android.ui.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.domain.identity.IdentityManager

@Composable
fun IdentityScreen(identityManager: IdentityManager) {
    val identity = identityManager.loadExistingIdentity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Identity",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        InfoCard(label = "Nostr npub", value = identity?.nostrNpub ?: "-")
        InfoCard(label = "EVM address", value = identity?.evmAddress ?: "-")
        InfoCard(label = "DID", value = identity?.did ?: "-")
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
