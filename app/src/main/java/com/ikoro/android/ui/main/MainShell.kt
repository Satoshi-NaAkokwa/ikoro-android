package com.ikoro.android.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ikoro.android.R
import com.ikoro.android.domain.identity.IdentityManager
import com.ikoro.android.ui.wallet.WalletScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    modifier: Modifier = Modifier,
    identityManager: IdentityManager,
    onResetIdentity: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onResetIdentity) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reset identity"
                        )
                    }
                }
            )
        }
    ) { padding ->
        WalletScreen(
            identityManager = identityManager,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
