package com.ikoro.android.ui.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.di.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen() {
    val walletManager = remember { ServiceLocator.walletManager() }
    val assets = remember { walletManager.listAssets() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wallet") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Multi-chain wallet",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Balance: ${walletManager.getBalance().getOrDefault("...")}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Assets:",
                style = MaterialTheme.typography.titleMedium
            )
            assets.forEach { asset ->
                Text(text = "• $asset", style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { /* send flow */ },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Send")
            }
        }
    }
}
