package com.ikoro.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IkoroColorScheme = darkColorScheme(
    primary = IkoroGold500,
    onPrimary = IkoroCharcoal,
    secondary = IkoroGreen500,
    onSecondary = IkoroOnSurface,
    background = IkoroSurface,
    onBackground = IkoroOnSurface,
    surface = IkoroCharcoal,
    onSurface = IkoroOnSurface,
    error = IkoroError
)

@Composable
fun IkoroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IkoroColorScheme,
        typography = IkoroTypography,
        content = content
    )
}
