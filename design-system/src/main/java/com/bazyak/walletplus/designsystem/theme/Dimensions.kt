package com.bazyak.walletplus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimension constants for the Wallet design system.
 *
 * Defines standard sizes for common UI elements like icons, buttons, and containers.
 * Use these values to ensure visual consistency across the app.
 *
 * Categories:
 * - Icon sizes: Small, medium, large icons
 * - Button heights: Standard button dimensions
 * - Container sizes: Common container dimensions
 * - Elevation: Standard elevation levels
 *
 * Usage:
 * ```
 * Icon(
 *     imageVector = Icons.Default.Add,
 *     contentDescription = null,
 *     modifier = Modifier.size(Dimensions.iconMedium)
 * )
 * ```
 */
object Dimensions {
    // ========== Icon Sizes ==========

    /**
     * Small icon size (16dp).
     * Use for inline icons, list item icons.
     */
    val iconSmall: Dp = 16.dp

    /**
     * Medium icon size (24dp).
     * Default icon size for buttons and toolbars.
     */
    val iconMedium: Dp = 24.dp

    /**
     * Large icon size (32dp).
     * Use for prominent icons, empty states.
     */
    val iconLarge: Dp = 32.dp

    /**
     * Extra large icon size (48dp).
     * Use for hero icons, feature illustrations.
     */
    val iconExtraLarge: Dp = 48.dp

    // ========== Button Dimensions ==========

    /**
     * Standard button height (48dp).
     * Meets minimum touch target size guidelines.
     */
    val buttonHeight: Dp = 48.dp

    /**
     * Small button height (36dp).
     * Use for compact layouts, secondary actions.
     */
    val buttonHeightSmall: Dp = 36.dp

    /**
     * Large button height (56dp).
     * Use for primary CTAs, prominent actions.
     */
    val buttonHeightLarge: Dp = 56.dp

    /**
     * Minimum button width (64dp).
     */
    val buttonMinWidth: Dp = 64.dp

    // ========== Container Dimensions ==========

    /**
     * Standard card elevation (2dp).
     */
    val cardElevation: Dp = 2.dp

    /**
     * Elevated card elevation (4dp).
     */
    val cardElevationElevated: Dp = 4.dp

    /**
     * Dialog elevation (6dp).
     */
    val dialogElevation: Dp = 6.dp

    /**
     * App bar height (64dp).
     */
    val appBarHeight: Dp = 64.dp

    /**
     * Bottom bar height (80dp).
     */
    val bottomBarHeight: Dp = 80.dp

    // ========== Pass Card Dimensions ==========

    /**
     * Minimum pass card width in detail/preview views (280dp).
     * Ensures readability on compact devices (~320dp screen width).
     */
    val passCardMinWidth: Dp = 280.dp

    /**
     * Maximum pass card width in detail/preview views (500dp).
     * Prevents cards from becoming unwieldy on tablets and foldables.
     */
    val passCardMaxWidth: Dp = 500.dp

    // ========== Misc ==========

    /**
     * Divider thickness (1dp).
     */
    val dividerThickness: Dp = 1.dp

    /**
     * Minimum touch target size (48dp).
     * Ensures accessibility compliance.
     */
    val minTouchTargetSize: Dp = 48.dp
}
