package com.ikoro.android.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ikoro.android.R
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.ui.calls.CallsScreen
import com.ikoro.android.ui.chat.ChatScreen
import com.ikoro.android.ui.identity.IdentityScreen
import com.ikoro.android.ui.market.MarketScreen
import com.ikoro.android.ui.wallet.WalletScreen

@Composable
private fun MainContent(selected: Int, identityManager: IdentityManager) {
    when (selected) {
        0 -> ChatScreen()
        1 -> WalletScreen()
        2 -> IdentityScreen(identityManager)
        3 -> MarketScreen()
        4 -> CallsScreen()
    }
}

private val items = listOf(
    Triple("chat", R.string.chat, Icons.Default.ChatBubble),
    Triple("wallet", R.string.wallet, Icons.Default.Wallet),
    Triple("identity", R.string.identity, Icons.Default.Person),
    Triple("market", R.string.market, Icons.Default.ShoppingCart),
    Triple("calls", R.string.calls, Icons.Default.Call)
)

@Composable
fun MainShell(modifier: Modifier = Modifier, identityManager: IdentityManager) {
    var selected by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.third, contentDescription = stringResource(item.second)) },
                        label = { Text(stringResource(item.second)) },
                        selected = selected == index,
                        onClick = { selected = index }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainContent(selected, identityManager)
            }
        }
    }
}
