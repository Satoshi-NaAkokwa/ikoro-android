package com.ikoro.android.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ikoro.android.R
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.identity.IdentityManager

@Composable
fun CreateIdentityScreen(
    identityManager: IdentityManager,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var identity by remember { mutableStateOf<Identity?>(null) }

    if (identity == null) {
        val result = remember { identityManager.createIdentity() }
        identity = result.getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.mnemonic_warning),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = identity?.mnemonic ?: "",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.confirm))
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}
