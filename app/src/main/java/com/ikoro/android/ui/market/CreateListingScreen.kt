package com.ikoro.android.ui.market

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
fun CreateListingScreen(onDone: () -> Unit) {
    val marketManager = remember { ServiceLocator.marketManager() }
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var asset by remember { mutableStateOf("RBTC") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create Listing") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = asset,
                onValueChange = { asset = it },
                label = { Text("Asset type") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = {
                    marketManager.createListing(title, price, asset)
                    onDone()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Post")
            }
        }
    }
}
