package com.ikoro.android.ui.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikoro.android.R
import com.ikoro.android.data.model.Asset
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.domain.wallet.WalletManager
import com.ikoro.android.ui.components.EmptyAnimations
import com.ikoro.android.ui.components.EmptyState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(identityManager: IdentityManager) {
    val walletManager = remember(identityManager) { WalletManager(identityManager) }
    val identityState: State<Identity?> = produceState<Identity?>(null) {
        value = identityManager.loadExistingIdentity()
    }
    val identity = identityState.value

    val assets = remember { mutableStateOf(listOf<Asset>()) }
    var sendAsset by remember { mutableStateOf<Asset?>(null) }

    var loadError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            assets.value = walletManager.loadAssets()
            loadError = ""
        } catch (e: Throwable) {
            Timber.e(e, "Wallet asset load crashed")
            loadError = "Could not load wallet: ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.wallet)) }) },
        floatingActionButton = {
            if (loadError.isBlank()) {
                ExtendedFloatingActionButton(
                    onClick = { sendAsset = assets.value.firstOrNull() },
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                    text = { Text("Send") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (identity == null) {
                EmptyState(
                    title = "No identity",
                    subtitle = "Create or restore your identity to use the wallet.",
                    animationRes = EmptyAnimations.wallet
                )
                return@Column
            }

            WalletHeader(identity.evmAddress, assets.value)
            Spacer(modifier = Modifier.height(16.dp))

            if (loadError.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = loadError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Text(
                text = "Assets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(assets.value, key = { it.id }) { asset ->
                    AssetRow(
                        asset = asset,
                        onClick = { sendAsset = asset }
                    )
                }
            }
        }
    }

    sendAsset?.let { asset ->
        SendSheet(
            asset = asset,
            walletManager = walletManager,
            onDismiss = { sendAsset = null }
        )
    }
}

@Composable
private fun WalletHeader(address: String, assets: List<Asset>) {
    val primary = assets.firstOrNull { it.isPrimary } ?: assets.firstOrNull()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = primary?.balance ?: "0 RBTC",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = primary?.fiatValue ?: "\u00240.00",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${address.take(6)}...${address.takeLast(4)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AssetRow(asset: Asset, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(asset)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.chainName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = asset.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = asset.balance,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = asset.fiatValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Box(asset: Asset) {
    val color = Color(asset.accentColor)
    Text(
        text = asset.symbol.first().toString(),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .padding(12.dp)
    )
}
