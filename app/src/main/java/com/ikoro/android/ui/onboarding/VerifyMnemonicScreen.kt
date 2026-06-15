package com.ikoro.android.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ikoro.android.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerifyMnemonicScreen(
    words: List<String>,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current

    val shuffled = remember(words) { words.shuffled() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.verify_seed_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.verify_seed_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (selected.isEmpty()) "Tap words below" else selected.joinToString(" "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            shuffled.forEach { word ->
                val alreadySelected = selected.contains(word)
                Button(
                    onClick = {
                        if (alreadySelected) return@Button
                        error = ""
                        selected = selected + word
                    },
                    enabled = !alreadySelected
                ) {
                    Text(word)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selected == words) {
                    onVerified()
                } else {
                    error = context.resources.getString(R.string.verification_failed)
                    selected = emptyList()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selected.size == words.size
        ) {
            Text(stringResource(R.string.confirm))
        }
        OutlinedButton(
            onClick = {
                selected = emptyList()
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}
