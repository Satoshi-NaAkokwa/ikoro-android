package com.ikoro.android.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ikoro.android.R
import com.ikoro.android.domain.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RestoreIdentityScreen(
    identityManager: IdentityManager,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var seed by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var restoring by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.restore_identity),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Enter your 12 or 24-word recovery phrase. One space between each word.",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = seed,
            onValueChange = { if (!restoring) { seed = it; error = "" } },
            label = { Text(stringResource(R.string.seed_input_hint)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 8,
            enabled = !restoring
        )
        if (error.isNotEmpty()) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                restoring = true
                error = ""
                scope.launch(Dispatchers.IO) {
                    val result = identityManager.restoreIdentity(seed)
                    withContext(Dispatchers.Main) {
                        restoring = false
                        result.fold(
                            onSuccess = { onDone() },
                            onFailure = { error = it.message ?: "Invalid phrase" }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = seed.isNotBlank() && !restoring
        ) {
            if (restoring) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.confirm))
            }
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !restoring
        ) {
            Text(stringResource(R.string.back))
        }
    }
}
