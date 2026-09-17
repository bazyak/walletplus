package com.bazyak.walletplus.designsystem.foundation.typography

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * Semantic text styles for the Wallet design system.
 *
 * Provides named text styles that convey semantic meaning (e.g., "primary" vs "secondary")
 * instead of using opacity modifiers like `.copy(alpha = 0.5)` throughout the codebase.
 *
 * Benefits:
 * - Consistent text hierarchy across the app
 * - Easy to adjust styling globally
 * - Clear semantic meaning in code
 *
 * Usage:
 * ```
 * Text(
 *     text = "Primary text",
 *     style = MaterialTheme.textStyles.bodyPrimary
 * )
 * Text(
 *     text = "Secondary text",
 *     style = MaterialTheme.textStyles.bodySecondary
 * )
 * ```
 */
@Immutable
data class SemanticTextStyles(
    // Body text styles
    val bodyPrimary: TextStyle,
    val bodySecondary: TextStyle,

    // Label text styles
    val labelPrimary: TextStyle,
    val labelSecondary: TextStyle,

    // Title text styles
    val titlePrimary: TextStyle,
    val titleSecondary: TextStyle,

    // Caption text styles (small, supporting text)
    val captionPrimary: TextStyle,
    val captionSecondary: TextStyle,
)

/**
 * CompositionLocal for accessing semantic text styles throughout the app.
 */
val LocalSemanticTextStyles = staticCompositionLocalOf<SemanticTextStyles> {
    error("No SemanticTextStyles provided")
}

/**
 * Extension property to access semantic text styles from MaterialTheme.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyComposable() {
 *     Text(
 *         text = "Example",
 *         style = MaterialTheme.textStyles.bodyPrimary
 *     )
 * }
 * ```
 */
val MaterialTheme.textStyles: SemanticTextStyles
    @Composable
    @ReadOnlyComposable
    get() = LocalSemanticTextStyles.current

/**
 * Create semantic text styles based on the current Material theme.
 *
 * Maps Material 3 typography and color scheme to semantic text styles.
 * Secondary styles use reduced opacity (0.7) for visual hierarchy.
 *
 * @return SemanticTextStyles configured with the current theme
 */
@Composable
fun createSemanticTextStyles(): SemanticTextStyles {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme

    return SemanticTextStyles(
        bodyPrimary = typography.bodyLarge.copy(
            color = colorScheme.onSurface,
        ),
        bodySecondary = typography.bodyLarge.copy(
            color = colorScheme.onSurface.copy(alpha = 0.7f),
        ),

        labelPrimary = typography.labelLarge.copy(
            color = colorScheme.onSurface,
        ),
        labelSecondary = typography.labelLarge.copy(
            color = colorScheme.onSurface.copy(alpha = 0.7f),
        ),

        titlePrimary = typography.titleLarge.copy(
            color = colorScheme.onSurface,
        ),
        titleSecondary = typography.titleLarge.copy(
            color = colorScheme.onSurface.copy(alpha = 0.7f),
        ),

        captionPrimary = typography.bodySmall.copy(
            color = colorScheme.onSurface,
        ),
        captionSecondary = typography.bodySmall.copy(
            color = colorScheme.onSurface.copy(alpha = 0.7f),
        ),
    )
}
