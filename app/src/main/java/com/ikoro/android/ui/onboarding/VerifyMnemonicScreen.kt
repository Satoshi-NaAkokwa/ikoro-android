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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    val context = LocalContext.current
    val checks = remember(words) { generateChecks(words) }
    var selections by remember { mutableStateOf(emptyMap<Int, String>()) }
    var error by remember { mutableStateOf("") }

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
            text = "Tap the correct word for each highlighted position from your backup.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        checks.forEach { (index, candidates) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Word #${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    candidates.forEach { word ->
                        val selected = selections[index] == word
                        OutlinedButton(
                            onClick = {
                                error = ""
                                selections = selections + (index to word)
                            }
                        ) {
                            Text(
                                word,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val allAnswered = checks.keys.all { selections.containsKey(it) }
        Button(
            onClick = {
                val correct = checks.all { (index, _) -> selections[index] == words[index] }
                if (correct) {
                    onVerified()
                } else {
                    error = context.resources.getString(R.string.verification_failed)
                    selections = emptyMap()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = allAnswered
        ) {
            Text(stringResource(R.string.confirm))
        }
        OutlinedButton(
            onClick = {
                selections = emptyMap()
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

private fun generateChecks(words: List<String>): Map<Int, List<String>> {
    val random = java.util.Random()
    val indices = words.indices.shuffled(random).take(3).sorted()
    return indices.associate { index ->
        val correct = words[index]
        val distractors = words.filterIndexed { i, _ -> i != index }.shuffled(random).take(3)
        val candidates = (distractors + correct).shuffled(random)
        index to candidates
    }
}
