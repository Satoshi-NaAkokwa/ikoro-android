package com.ikoro.android.ui.market

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ikoro.android.R
import com.ikoro.android.ui.components.EmptyAnimations
import com.ikoro.android.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen() {
    val tabs = listOf(R.string.p2p_tab, R.string.airtime_tab, R.string.tickets_tab, R.string.savings_tab, R.string.land_tab)
    var selected by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.market)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selected,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, titleRes ->
                    Tab(
                        selected = selected == index,
                        onClick = { selected = index },
                        text = {
                            Text(
                                text = stringResource(titleRes),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }
            when (selected) {
                0 -> P2PExchangeTab()
                1 -> AirtimeTab()
                2 -> TicketsTab()
                3 -> SavingsTab()
                4 -> LandRegistryTab()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun P2PExchangeTab() {
    var showSheet by remember { mutableStateOf(false) }

    EmptyState(
        title = "No P2P listings yet",
        subtitle = "Buy or sell crypto directly with other Ikoro users.",
        animationRes = EmptyAnimations.market,
        actionLabel = "Create offer",
        onAction = { showSheet = true }
    )

    if (showSheet) {
        CreateOfferSheet(onDismiss = { showSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOfferSheet(onDismiss: () -> Unit) {
    var asset by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Create P2P offer",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = asset,
                onValueChange = { asset = it },
                label = { Text("Asset (e.g. RBTC, USDT)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price per unit (USD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // TODO: publish offer to backend / smart contract
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = asset.isNotBlank() && amount.isNotBlank() && price.isNotBlank()
            ) {
                Text("Preview offer")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AirtimeTab() {
    EmptyState(
        title = "Airtime & Data",
        subtitle = "Pay with BTC, Lightning, or stablecoins. Coming soon.",
        animationRes = EmptyAnimations.market
    )
}

@Composable
private fun TicketsTab() {
    EmptyState(
        title = "Event Tickets",
        subtitle = "NFT-based tickets and verification. Coming soon.",
        animationRes = EmptyAnimations.market
    )
}

@Composable
private fun SavingsTab() {
    EmptyState(
        title = "Savings Groups",
        subtitle = "Ajo / Esusu / Stokvel rotating savings. Coming soon.",
        animationRes = EmptyAnimations.market
    )
}

@Composable
private fun LandRegistryTab() {
    EmptyState(
        title = "Land Registry",
        subtitle = "Tokenized land titles and community records. Coming soon.",
        animationRes = EmptyAnimations.market
    )
}
