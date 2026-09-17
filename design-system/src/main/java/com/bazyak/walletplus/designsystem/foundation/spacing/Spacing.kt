package com.bazyak.walletplus.designsystem.foundation.spacing

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale for the Wallet design system.
 *
 * Provides a consistent spacing system based on a 4dp grid.
 * Use these values for padding, margins, and gaps throughout the app.
 *
 * Scale:
 * - none: 0dp (no spacing)
 * - extraSmall: 4dp (minimal spacing)
 * - small: 8dp (tight spacing)
 * - medium: 12dp (default spacing)
 * - mediumLarge: 16dp (comfortable spacing)
 * - large: 20dp (generous spacing)
 * - extraLarge: 24dp (section spacing)
 * - huge: 32dp (large section spacing)
 * - massive: 48dp (screen-level spacing)
 *
 * Usage:
 * ```
 * Box(
 *     modifier = Modifier.padding(MaterialTheme.spacing.medium)
 * )
 * ```
 */
@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val mediumLarge: Dp = 16.dp,
    val large: Dp = 20.dp,
    val extraLarge: Dp = 24.dp,
    val huge: Dp = 32.dp,
    val massive: Dp = 48.dp,
)

/**
 * CompositionLocal for accessing spacing values throughout the app.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Extension property to access spacing from MaterialTheme.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyComposable() {
 *     Box(
 *         modifier = Modifier.padding(MaterialTheme.spacing.medium)
 *     )
 * }
 * ```
 */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
