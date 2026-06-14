package com.ikoro.android.ui.identity

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ikoro.android.data.model.Identity
import com.ikoro.android.di.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsScreen(identity: Identity?) {
    val ssiManager = ServiceLocator.ssiManager()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Credentials") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "SSI Credential Wallet",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "DID: ${identity?.did ?: "..."}",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = {
                    identity?.let { ssiManager.issueSelfCredential(it) }
                }
            ) {
                Text("Issue Self-Credential")
            }
        }
    }
}
