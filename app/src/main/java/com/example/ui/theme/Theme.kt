package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IlamWhite,
    onPrimary = IlamBlack,
    primaryContainer = IlamSurfaceDark,
    onPrimaryContainer = IlamWhite,
    secondary = IlamGold,
    onSecondary = IlamBlack,
    secondaryContainer = IlamCardDark,
    onSecondaryContainer = IlamGoldLight,
    tertiary = IlamGoldLight,
    background = IlamBlack,
    onBackground = IlamWhite,
    surface = IlamDark,
    onSurface = IlamWhite,
    surfaceVariant = IlamSurfaceDark,
    onSurfaceVariant = IlamTextMuted,
    outline = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = IlamBlack,
    onPrimary = IlamWhite,
    primaryContainer = IlamDark,
    onPrimaryContainer = IlamWhite,
    secondary = IlamGoldDark,
    onSecondary = IlamWhite,
    secondaryContainer = IlamGoldLight,
    onSecondaryContainer = IlamBlack,
    tertiary = IlamGold,
    background = IlamOffWhite,
    onBackground = IlamTextPrimary,
    surface = IlamWhite,
    onSurface = IlamTextPrimary,
    surfaceVariant = IlamSurfaceLight,
    onSurfaceVariant = IlamTextSecondary,
    outline = IlamBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to pristine luxury light theme with rich dark accents
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

