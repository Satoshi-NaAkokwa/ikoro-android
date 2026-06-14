package com.ikoro.android.ui.identity

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
import com.ikoro.android.data.model.Identity
import com.ikoro.android.di.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenIssuanceScreen(identity: Identity?) {
    val ssiManager = remember { ServiceLocator.ssiManager() }
    var community by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Community Token") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Issue community tokens from your identity.")
            OutlinedTextField(
                value = community,
                onValueChange = { community = it },
                label = { Text("Community name") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = {
                    identity?.let {
                        ssiManager.issueCommunityToken(it, community, amount.toLongOrNull() ?: 0)
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Issue")
            }
        }
    }
}
