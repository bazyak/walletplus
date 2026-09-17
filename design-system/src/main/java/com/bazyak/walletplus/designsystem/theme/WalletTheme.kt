package com.bazyak.walletplus.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.bazyak.walletplus.designsystem.foundation.color.LocalWalletColors
import com.bazyak.walletplus.designsystem.foundation.spacing.LocalSpacing
import com.bazyak.walletplus.designsystem.foundation.spacing.Spacing
import com.bazyak.walletplus.designsystem.foundation.typography.LocalSemanticTextStyles
import com.bazyak.walletplus.designsystem.foundation.typography.createSemanticTextStyles

/**
 * Main theme composable for the Wallet app.
 *
 * This composable orchestrates the entire design system by:
 * - Applying Material 3 theming with custom color schemes
 * - Providing CompositionLocals for custom design tokens
 * - Supporting automatic dark/light theme switching
 *
 * Features:
 * - Black & White base colors with Blue (#FF0077b6) primary/accent
 * - No dynamic color support (uses fixed color schemes)
 * - Complete Material 3 typography scale
 * - Custom pass color palettes
 * - Semantic text styles
 * - Consistent spacing system
 *
 * Usage:
 * ```
 * @Composable
 * fun MyApp() {
 *     WalletTheme {
 *         // Your app content
 *     }
 * }
 * ```
 *
 * Accessing custom design tokens:
 * ```
 * @Composable
 * fun MyComposable() {
 *     // Spacing
 *     val spacing = MaterialTheme.spacing.medium
 *
 *     // Pass colors
 *     val passColors = MaterialTheme.walletColors
 *
 *     // Semantic text styles
 *     Text(
 *         text = "Example",
 *         style = MaterialTheme.textStyles.bodyPrimary
 *     )
 * }
 * ```
 *
 * @param darkTheme Whether to use dark theme. Defaults to system theme.
 * @param content The content to theme.
 */
@Composable
fun WalletTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // Select color scheme based on theme (no dynamic color support)
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Provide all design system tokens via CompositionLocals
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalWalletColors provides com.bazyak.walletplus.designsystem.foundation.color.DefaultPassColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WalletTypography,
            shapes = WalletShapes,
        ) {
            // Create and provide semantic text styles based on current theme
            val semanticTextStyles = createSemanticTextStyles()

            CompositionLocalProvider(
                LocalSemanticTextStyles provides semanticTextStyles,
                content = content,
            )
        }
    }
}
