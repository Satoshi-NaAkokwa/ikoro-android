package com.ikoro.android.ui.market

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.ikoro.android.BuildConfig
import com.ikoro.android.di.ServiceLocator
import kotlinx.coroutines.launch

@Composable
fun AirtimePayDialog(
    context: android.content.Context,
    providers: List<String>,
    onDismiss: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf(providers.firstOrNull() ?: "") }
    val scope = rememberCoroutineScope()
    val contractService = remember(context) { ServiceLocator.thirdwebContractService(context) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Pay for airtime / data") },
        text = {
            Column {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (local currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Provider: ${selectedProvider.ifBlank { "Select" }}")
                if (result.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result)
                }
                if (busy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isBlank() || amount.isBlank()) {
                        result = "Enter phone and amount"
                        return@Button
                    }
                    scope.launch {
                        busy = true
                        result = "Submitting to Ikoro Airtime escrow..."
                        val actionResult = contractService.executeAction(
                            action = "airtime",
                            params = mapOf("phone" to phone, "amount" to amount, "provider" to selectedProvider)
                        )
                        result = actionResult.fold(
                            onSuccess = { "Order submitted: $it" },
                            onFailure = { "Error: ${it.message}" }
                        )
                        busy = false
                    }
                },
                enabled = !busy
            ) {
                Text("Pay now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        }
    )
}

@Composable
fun TicketBuyDialog(
    context: android.content.Context,
    event: TicketEvent,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    val scope = rememberCoroutineScope()
    val contractService = remember(context) { ServiceLocator.thirdwebContractService(context) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Buy tickets: ${event.title}") },
        text = {
            Column {
                Text("${event.location} • ${event.date}")
                Text("Price: ${event.price}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                if (result.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result)
                }
                if (busy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        result = "Minting ticket NFT..."
                        val actionResult = contractService.executeAction(
                            action = "ticket",
                            params = mapOf("event" to event.title, "quantity" to quantity)
                        )
                        result = actionResult.fold(
                            onSuccess = { "Tickets issued: $it" },
                            onFailure = { "Error: ${it.message}" }
                        )
                        busy = false
                    }
                },
                enabled = !busy
            ) {
                Text("Buy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        }
    )
}

@Composable
fun SavingsCreateDialog(
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contribution by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val contractService = remember(context) { ServiceLocator.thirdwebContractService(context) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Create savings group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contribution,
                    onValueChange = { contribution = it },
                    label = { Text("Contribution amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of members") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                if (result.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result)
                }
                if (busy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || contribution.isBlank() || members.isBlank()) {
                        result = "Fill all fields"
                        return@Button
                    }
                    scope.launch {
                        busy = true
                        result = "Creating on-chain ROSCA group..."
                        val actionResult = contractService.executeAction(
                            action = "savings",
                            params = mapOf("name" to name, "contribution" to contribution, "members" to members)
                        )
                        result = actionResult.fold(
                            onSuccess = { "Group created: $it" },
                            onFailure = { "Error: ${it.message}" }
                        )
                        busy = false
                    }
                },
                enabled = !busy
            ) {
                Text("Create group")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        }
    )
}

@Composable
fun LandRegisterDialog(
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    var geoHash by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val contractService = remember(context) { ServiceLocator.thirdwebContractService(context) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Register land parcel") },
        text = {
            Column {
                OutlinedTextField(
                    value = geoHash,
                    onValueChange = { geoHash = it },
                    label = { Text("Geo hash / parcel ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Owner name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                if (result.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result)
                }
                if (busy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (geoHash.isBlank() || owner.isBlank()) {
                        result = "Fill all fields"
                        return@Button
                    }
                    scope.launch {
                        busy = true
                        result = "Minting land title NFT..."
                        val actionResult = contractService.executeAction(
                            action = "land",
                            params = mapOf("geoHash" to geoHash, "owner" to owner)
                        )
                        result = actionResult.fold(
                            onSuccess = { "Title registered: $it" },
                            onFailure = { "Error: ${it.message}" }
                        )
                        busy = false
                    }
                },
                enabled = !busy
            ) {
                Text("Register parcel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        }
    )
}

// Local model used by dialogs
data class TicketEvent(
    val title: String,
    val location: String,
    val date: String,
    val price: String,
    val sold: String
)
