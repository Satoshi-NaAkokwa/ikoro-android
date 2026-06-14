package com.ikoro.android.ui.market

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.di.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen() {
    val tabs = listOf("P2P", "Airtime", "Tickets", "Savings", "Land")
    var selected by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Market") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selected) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selected == index,
                        onClick = { selected = index },
                        text = { Text(title) }
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

@Composable
private fun P2PExchangeTab() {
    val marketManager = remember { ServiceLocator.marketManager() }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("P2P Exchange", style = MaterialTheme.typography.titleLarge)
        Text("No listings yet.", modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun AirtimeTab() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Airtime & Data", style = MaterialTheme.typography.titleLarge)
        Text("Pay with BTC, L-USDT, or stablecoins.", modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun TicketsTab() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Event Tickets", style = MaterialTheme.typography.titleLarge)
        Text("NFT-based tickets and verification.", modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SavingsTab() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Savings Groups", style = MaterialTheme.typography.titleLarge)
        Text("Ajo / Esusu / Stokvel rotating savings.", modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun LandRegistryTab() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Land Registry", style = MaterialTheme.typography.titleLarge)
        Text("Tokenized land titles and community records.", modifier = Modifier.padding(top = 8.dp))
    }
}
