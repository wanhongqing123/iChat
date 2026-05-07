package com.ichat.login.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val IChatColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BgSurface,
    background = BgPage,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    error = StateError,
)

@Composable
fun IChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IChatColorScheme,
        typography = IChatTypography,
        content = content,
    )
}
