package com.bazyak.walletplus.designsystem.foundation.color

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Pass color palette data class.
 *
 * Represents a color pair for custom pass cards:
 * - background: Refined color for the card background
 * - foreground: Darker variant for text and accents
 * - name: Display name for the color (e.g., "Red", "Blue")
 *
 * @property background The card background color
 * @property foreground The darker foreground/text color
 * @property name The human-readable name of the color
 */
@Immutable
data class PassColorPalette(val background: Color, val foreground: Color, val name: String)

/**
 * Default pass color palettes.
 *
 * A collection of custom pass card colors with corresponding darker foreground colors.
 * The palettes mix brighter and calmer options while keeping text contrast above WCAG AA.
 */
val DefaultPassColors = listOf(
    PassColorPalette(
        background = Color(0xFFF7C8CF),
        foreground = Color(0xFF7A2030),
        name = "Rose",
    ),
    PassColorPalette(
        background = Color(0xFFFFC3A6),
        foreground = Color(0xFF7C2D12),
        name = "Coral",
    ),
    PassColorPalette(
        background = Color(0xFFF6D77A),
        foreground = Color(0xFF634400),
        name = "Amber",
    ),
    PassColorPalette(
        background = Color(0xFFC7D9A7),
        foreground = Color(0xFF365314),
        name = "Sage",
    ),
    PassColorPalette(
        background = Color(0xFFB7E1CE),
        foreground = Color(0xFF0F4A3C),
        name = "Mint",
    ),
    PassColorPalette(
        background = Color(0xFF96D4D0),
        foreground = Color(0xFF064E4A),
        name = "Teal",
    ),
    PassColorPalette(
        background = Color(0xFFB8D8F2),
        foreground = Color(0xFF123A5A),
        name = "Sky",
    ),
    PassColorPalette(
        background = Color(0xFFAFC4FF),
        foreground = Color(0xFF1E2A78),
        name = "Cobalt",
    ),
    PassColorPalette(
        background = Color(0xFFD6C6F2),
        foreground = Color(0xFF4B2E83),
        name = "Lavender",
    ),
    PassColorPalette(
        background = Color(0xFFE7B8D8),
        foreground = Color(0xFF6B214F),
        name = "Plum",
    ),
    PassColorPalette(
        background = Color(0xFFD8D2C4),
        foreground = Color(0xFF3F3A33),
        name = "Stone",
    ),
    PassColorPalette(
        background = Color(0xFFBFC7D1),
        foreground = Color(0xFF263241),
        name = "Graphite",
    ),
)

/**
 * CompositionLocal for accessing pass color palettes throughout the app.
 *
 * Usage:
 * ```
 * val passColors = MaterialTheme.walletColors
 * ```
 */
val LocalWalletColors = staticCompositionLocalOf { DefaultPassColors }

/**
 * Extension property to access wallet pass colors from MaterialTheme.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyComposable() {
 *     val passColors = MaterialTheme.walletColors
 *     // Use passColors[0].background, passColors[0].foreground, etc.
 * }
 * ```
 */
val MaterialTheme.walletColors: List<PassColorPalette>
    @Composable
    @ReadOnlyComposable
    get() = LocalWalletColors.current
