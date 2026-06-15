package com.ikoro.android.ui.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ikoro.android.data.model.Asset
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.domain.wallet.WalletManager
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSheet(
    asset: Asset,
    walletManager: WalletManager,
    identityManager: IdentityManager,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var to by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Send ${asset.symbol} on ${asset.chainName}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = to,
                onValueChange = { to = it },
                label = { Text("Recipient address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )
            if (status.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status,
                    color = if (status.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        status = "Deriving key and signing..."
                        val keyPair = identityManager.deriveEvmKeyPair()
                        if (keyPair.isFailure) {
                            status = "Error: ${keyPair.exceptionOrNull()?.message}"
                            busy = false
                            return@launch
                        }
                        val credentials = Credentials.create(keyPair.getOrThrow())
                        val result = walletManager.send(asset.id, credentials, to, amount)
                        status = if (result.isSuccess) {
                            "Sent: ${result.getOrThrow()}"
                        } else {
                            "Error: ${result.exceptionOrNull()?.message}"
                        }
                        busy = false
                    }
                },
                enabled = to.isNotBlank() && amount.isNotBlank() && !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
