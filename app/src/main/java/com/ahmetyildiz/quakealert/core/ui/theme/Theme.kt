package com.ahmetyildiz.quakealert.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun QuakeAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val severityColors: SeverityColors = if (darkTheme) DarkSeverityColors else LightSeverityColors
    CompositionLocalProvider(LocalSeverityColors provides severityColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QuakeAlertTypography,
            shapes = QuakeAlertShapes,
            content = content,
        )
    }
}

object QuakeAlertTheme {
    val severityColors: SeverityColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSeverityColors.current
}
