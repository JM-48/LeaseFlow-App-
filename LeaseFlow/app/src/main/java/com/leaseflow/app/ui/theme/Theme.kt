package com.leaseflow.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ========== ESQUEMA DE COLORES LEASEFLOW ==========
// Basado en el diseño del documento: Verde oscuro profesional

private val DarkColorScheme = darkColorScheme(
    primary = LfPurple2,
    onPrimary = White,
    primaryContainer = LfNavy2,
    onPrimaryContainer = White,
    secondary = LfPurple,
    onSecondary = White,
    secondaryContainer = LfNavy,
    onSecondaryContainer = White,
    tertiary = LfCyan,
    onTertiary = LfNavy,
    background = LfNavy,
    onBackground = White,
    surface = LfNavy2,
    onSurface = White,
    surfaceVariant = LfNavy2,
    onSurfaceVariant = White.copy(alpha = 0.78f),
    outline = White.copy(alpha = 0.18f),
    error = LfError,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = LfPurple,
    onPrimary = White,
    primaryContainer = LfNavy,
    onPrimaryContainer = White,
    secondary = LfPurple2,
    onSecondary = White,
    secondaryContainer = LfLavender,
    onSecondaryContainer = LfNavy,
    tertiary = LfCyan,
    onTertiary = LfNavy,
    background = LfBg,
    onBackground = LfText,
    surface = LfSurface,
    onSurface = LfText,
    surfaceVariant = LfLavender,
    onSurfaceVariant = LfMuted,
    outline = LfBorder,
    error = LfError,
    onError = White,
    errorContainer = LfError.copy(alpha = 0.10f),
    onErrorContainer = LfError
)

@Composable
fun LeaseFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,  // Deshabilitado para mantener identidad de marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
