package com.ikoro.android.ui.onboarding

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ikoro.android.R
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateIdentityScreen(
    identityManager: IdentityManager,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<CreateState>(CreateState.Ready) }

    when (val s = state) {
        is CreateState.Ready -> GenerateStep(
            onGenerate = {
                state = CreateState.Generating
                scope.launch(Dispatchers.IO) {
                    val result = identityManager.createIdentity()
                    withContext(Dispatchers.Main) {
                        state = if (result.isSuccess) {
                            CreateState.DisplaySeed(result.getOrThrow())
                        } else {
                            val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                            CreateState.Error(error)
                        }
                    }
                }
            },
            onBack = onBack
        )
        is CreateState.Generating -> Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Creating your identity…")
        }
        is CreateState.Error -> ErrorStep(
            message = s.message,
            onRetry = { state = CreateState.Ready },
            onRestore = { state = CreateState.GoRestore }
        )
        is CreateState.GoRestore -> RestoreIdentityScreen(
            identityManager = identityManager,
            onDone = onDone,
            onBack = { state = CreateState.Ready }
        )
        is CreateState.DisplaySeed -> SeedStep(
            identity = s.identity,
            _identityManager = identityManager,
            onVerified = { state = CreateState.Verifying(s.identity) },
            onBack = onBack
        )
        is CreateState.Verifying -> VerifyMnemonicScreen(
            words = s.identity.mnemonic.split(" "),
            onVerified = onDone,
            onBack = { state = CreateState.DisplaySeed(s.identity) }
        )
    }
}

private sealed class CreateState {
    object Ready : CreateState()
    object Generating : CreateState()
    data class DisplaySeed(val identity: Identity) : CreateState()
    object GoRestore : CreateState()
    data class Error(val message: String) : CreateState()
    data class Verifying(val identity: Identity) : CreateState()
}

@Composable
private fun GenerateStep(onGenerate: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.create_identity),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ikoro will generate a single 24-word recovery phrase. That phrase secures your chat, wallet and identity across all chains.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate secure identity")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun ErrorStep(message: String, onRetry: () -> Unit, onRestore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Could not create identity",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try again")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRestore,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore from seed instead")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeedStep(
    identity: Identity,
    _identityManager: IdentityManager,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var confirmed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(R.string.mnemonic_warning),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    identity.mnemonic.split(" ").forEachIndexed { index, word ->
                        WordChip(index = index + 1, word = word)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Ikoro seed", identity.mnemonic))
                Toast.makeText(context, "Seed copied — clear clipboard after backup", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.copy_seed))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { confirmed = !confirmed }
                .padding(vertical = 8.dp)
        ) {
            Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
            Text(
                text = "I have written these words down in the correct order and stored them offline.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onVerified,
            modifier = Modifier.fillMaxWidth(),
            enabled = confirmed
        ) {
            Text(stringResource(R.string.i_wrote_it_down))
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun WordChip(index: Int, word: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "$index. $word",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
