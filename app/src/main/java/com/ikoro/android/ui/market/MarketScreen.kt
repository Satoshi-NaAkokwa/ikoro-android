package com.ikoro.android.ui.market

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ikoro.android.BuildConfig
import com.ikoro.android.R
import com.ikoro.android.di.ServiceLocator
import com.ikoro.android.ui.components.EmptyAnimations
import com.ikoro.android.ui.components.EmptyState
import kotlinx.coroutines.launch

private val Obsidian = Color(0xFF0B0B0F)
private val Gold = Color(0xFFF2C94C)
private val Success = Color(0xFF27AE60)
private val Error = Color(0xFFEB5757)
private val Neutral = Color(0xFFA0A0A8)
private val Surface = Color(0xFF141419)

private data class MarketTab(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen() {
    val tabs = listOf(
        MarketTab("P2P", Icons.Default.SwapHoriz),
        MarketTab("Airtime", Icons.Default.AirplanemodeActive),
        MarketTab("Tickets", Icons.Default.Event),
        MarketTab("Savings", Icons.Default.Savings),
        MarketTab("Land", Icons.Default.Landscape)
    )
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.market),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
            )
        },
        containerColor = Obsidian
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Obsidian)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selected,
                edgePadding = 16.dp,
                containerColor = Obsidian,
                contentColor = Gold
            ) {
                tabs.forEachIndexed { index, tab ->
                    val active = selected == index
                    Tab(
                        selected = active,
                        onClick = { selected = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (active) Gold else Neutral,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = tab.title,
                                color = if (active) Gold else Neutral,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Neutral.copy(alpha = 0.2f))
            )
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "market-tab"
            ) { target ->
                when (target) {
                    0 -> P2PExchangeTab()
                    1 -> AirtimeTab()
                    2 -> TicketsTab()
                    3 -> SavingsTab()
                    4 -> LandRegistryTab()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun P2PExchangeTab() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val contractService = remember(context) { ServiceLocator.thirdwebContractService(context) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var offers by remember { mutableStateOf(listOf<P2POffer>()) }
    var filter by remember { mutableStateOf("All") }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (offers.isEmpty()) {
                EmptyState(
                    title = "No P2P listings yet",
                    subtitle = if (BuildConfig.MARKETPLACE_CONTRACT_ADDRESS.isBlank()) {
                        "Marketplace contract address not configured. Offers will be on-chain once deployed."
                    } else {
                        "Buy or sell crypto directly with other Ikoro users."
                    },
                    animationRes = EmptyAnimations.market,
                    actionLabel = "Create offer",
                    onAction = { showSheet = true }
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(listOf("All", "Buy", "Sell", "BTC", "USDT", "aNGN")) { chip ->
                        FilterChip(
                            selected = filter == chip,
                            onClick = { filter = chip },
                            label = { Text(chip, color = if (filter == chip) Obsidian else Color.White) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(offers) { offer ->
                        P2POfferCard(offer)
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            CreateOfferSheet(
                onDismiss = { showSheet = false },
                onPost = { giveToken, wantToken, giveAmount, wantAmount, expiryHours ->
                    scope.launch {
                        val result = contractService.createListing(
                            chainId = "rootstock",
                            asset = giveToken,
                            amount = giveAmount,
                            price = wantAmount
                        )
                        result.onSuccess { txHash ->
                            snackbarHostState.showSnackbar("Offer posted: $txHash")
                        }.onFailure { error ->
                            snackbarHostState.showSnackbar(error.message ?: "Offer failed")
                        }
                    }
                }
            )
        }
    }
}

private data class P2POffer(
    val pair: String,
    val rate: String,
    val trader: String,
    val limits: String,
    val side: String
)

@Composable
private fun P2POfferCard(offer: P2POffer) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(offer.pair, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Rate: ${offer.rate}", color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text("Trader: ${offer.trader}", color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text("Limits: ${offer.limits}", color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
            Button(
                onClick = { },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (offer.side == "Buy") "Buy" else "Sell", color = Obsidian)
            }
        }
    }
}

@Composable
private fun CreateOfferSheet(
    onDismiss: () -> Unit,
    onPost: (giveToken: String, wantToken: String, giveAmount: String, wantAmount: String, expiryHours: String) -> Unit
) {
    var giveToken by remember { mutableStateOf("") }
    var wantToken by remember { mutableStateOf("") }
    var giveAmount by remember { mutableStateOf("") }
    var wantAmount by remember { mutableStateOf("") }
    var expiryHours by remember { mutableStateOf("24") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Create P2P Offer",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        TokenTextField("Give token", giveToken) { giveToken = it }
        TokenTextField("Give amount", giveAmount, KeyboardType.Decimal) { giveAmount = it }
        TokenTextField("Want token", wantToken) { wantToken = it }
        TokenTextField("Want amount", wantAmount, KeyboardType.Decimal) { wantAmount = it }
        TokenTextField("Expires in hours", expiryHours, KeyboardType.Number) { expiryHours = it }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                onPost(giveToken, wantToken, giveAmount, wantAmount, expiryHours)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = giveToken.isNotBlank() && wantToken.isNotBlank() &&
                    giveAmount.isNotBlank() && wantAmount.isNotBlank()
        ) {
            Text("Post offer", color = Obsidian, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AirtimeTab() {
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    val providers = listOf("MTN", "Airtel", "Glo", "9mobile")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Airtime & Data", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number", color = Neutral) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Text("Provider", color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(providers) { provider ->
                ProviderChip(provider, selectedProvider == provider) { selectedProvider = provider }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (local currency)", color = Neutral) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Airtime value", "₦${amount.ifEmpty { "0" }}")
                SummaryRow("Network fee", "0.5%")
                SummaryRow("Crypto total", "≈ 0.0012 RBTC", isTotal = true)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pay now", color = Obsidian, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProviderChip(name: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Gold else Surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(name, color = if (selected) Obsidian else Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TicketsTab() {
    val events = listOf(
        TicketEvent("Afrobeat Festival", "Lagos", "Sat, 15 Jul", "0.05 RBTC", "120 sold"),
        TicketEvent("Tech Summit", "Abuja", "Mon, 24 Jul", "0.02 RBTC", "45 sold"),
        TicketEvent("Community Football", "Enugu", "Sun, 30 Jul", "0.01 RBTC", "200 sold")
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Events & Tickets", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events) { event ->
                EventCard(event)
            }
        }
    }
}

private data class TicketEvent(val title: String, val location: String, val date: String, val price: String, val sold: String)

@Composable
private fun EventCard(event: TicketEvent) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Gold.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Event, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${event.location} • ${event.date}", color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text(event.sold, color = Success, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
            Text(event.price, color = Gold, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SavingsTab() {
    val groups = listOf(
        SavingsGroup("Family Ajo", "10 members", "₦5,000/week", "Next payout: Mon"),
        SavingsGroup("Tech Stokvel", "6 members", "0.01 RBTC/round", "Next payout: Fri")
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ajo / Esusu / Stokvel", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(groups) { group ->
                SavingsCard(group)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Create group", color = Gold)
        }
    }
}

private data class SavingsGroup(val name: String, val members: String, val contribution: String, val next: String)

@Composable
private fun SavingsCard(group: SavingsGroup) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Gold.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Savings, contentDescription = null, tint = Gold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(group.members, color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text(group.contribution, color = Gold, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            }
            Text(group.next, color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun LandRegistryTab() {
    val parcels = listOf(
        LandParcel("s14kwh2", "Community attested", "Alice"),
        LandParcel("s14kwh3", "Legal + AI", "Bob")
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Land Titles", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(parcels) { parcel ->
                LandCard(parcel)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Register parcel", color = Gold)
        }
    }
}

private data class LandParcel(val geoHash: String, val badge: String, val owner: String)

@Composable
private fun LandCard(parcel: LandParcel) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Success.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Landscape, contentDescription = null, tint = Success)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Geo: ${parcel.geoHash}", color = Color.White, fontWeight = FontWeight.Bold)
                Text(parcel.badge, color = Success, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text("Owner: ${parcel.owner}", color = Neutral, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Neutral)
        Text(value, color = if (isTotal) Gold else Color.White, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun TokenTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Neutral) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        singleLine = true
    )
}
