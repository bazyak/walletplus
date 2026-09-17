package com.bazyak.walletplus.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Light color scheme for the Wallet app.
 *
 * Features:
 * - White background (#FFFFFF)
 * - Black text (#000000)
 * - Blue primary/accent (#FF0077b6)
 * - Red error (#FFB3261E)
 */
internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1850C3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF90CAF9),
    onPrimaryContainer = Color(0xFF001D35),

    secondary = Color(0xFF535E68),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3EE),
    onSecondaryContainer = Color(0xFF101C24),

    tertiary = Color(0xFF6B5778),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = Color.White,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),

    scrim = Color(0x99000000),

    inverseSurface = Color(0xFF2E3135),
    inverseOnSurface = Color(0xFFF0F0F3),
    inversePrimary = Color(0xFF76C1FF),
)

/**
 * Dark color scheme for the Wallet app.
 *
 * Features:
 * - Black background (#000000)
 * - White text (#FFFFFF)
 * - Blue primary/accent (#FF0077b6)
 * - Red error (#F2B8B5)
 */
internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1850C3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFCAE6FF),

    secondary = Color(0xFFBBC7D2),
    onSecondary = Color(0xFF26313A),
    secondaryContainer = Color(0xFF3C4751),
    onSecondaryContainer = Color(0xFFD7E3EE),

    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFF52405F),
    onTertiaryContainer = Color(0xFFF2DAFF),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    background = Color.Black,
    onBackground = Color.White,

    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFC4C6D0),

    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF44474F),

    scrim = Color(0x99000000),

    inverseSurface = Color(0xFFE1E2E5),
    inverseOnSurface = Color(0xFF2E3135),
    inversePrimary = Color(0xFF0077b6),
)
