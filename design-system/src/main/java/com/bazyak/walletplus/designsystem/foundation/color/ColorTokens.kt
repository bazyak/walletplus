package com.bazyak.walletplus.designsystem.foundation.color

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens for the Wallet design system.
 *
 * These tokens provide semantic meaning to colors used throughout the app.
 * They map to Material 3 color roles and ensure consistent usage across components.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyComposable() {
 *     Box(
 *         modifier = Modifier.background(ColorTokens.surfaceDefault),
 *         contentColor = ColorTokens.contentPrimary
 *     )
 * }
 * ```
 */
object ColorTokens {
    /**
     * Primary brand color - used for main actions and key UI elements.
     */
    val brandPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    /**
     * Content on primary brand color.
     */
    val brandOnPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onPrimary

    /**
     * Default surface color - used for card backgrounds and elevated surfaces.
     */
    val surfaceDefault: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface

    /**
     * Variant surface color - used for alternate surface elevations.
     */
    val surfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceVariant

    /**
     * Primary content color - used for main text and icons.
     */
    val contentPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    /**
     * Secondary content color - used for subdued text and secondary icons.
     */
    val contentSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurfaceVariant

    /**
     * Error color - used for error states and destructive actions.
     */
    val error: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error

    /**
     * Content on error color.
     */
    val onError: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onError

    /**
     * Border color - used for outlines and dividers.
     */
    val border: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline

    /**
     * Variant border color - used for subtle borders and dividers.
     */
    val borderVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outlineVariant

    /**
     * Scrim color - used for modal overlays and dimming.
     */
    val scrim: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.scrim

    /**
     * Delete zone background while a pass is hovering over it.
     */
    val deleteZoneHoverBackground: Color = Color.Red

    /**
     * Delete zone background while idle.
     */
    val deleteZoneIdleBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceVariant

    /**
     * Delete zone icon and text color.
     */
    val deleteZoneContent: Color = Color.White

    /**
     * Fixed surface used for PKPass back-side paper cards.
     */
    val pkPassBackSurface: Color = Color.White

    /**
     * Fixed text color used on PKPass back-side paper cards.
     */
    val pkPassBackContent: Color = Color.Black

    /**
     * Fixed link color used on PKPass back-side paper cards.
     */
    val pkPassBackLink: Color = Color(0xFF2196F3)
}
