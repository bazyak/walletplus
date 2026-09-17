package com.bazyak.walletplus.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape system for the Wallet design system.
 *
 * Defines Material 3 shape tokens with consistent corner radii across the app.
 * These shapes are used by Material 3 components like Card, Button, TextField, etc.
 *
 * Material 3 shape categories:
 * - extraSmall: Small components (chips, small buttons)
 * - small: Standard components (buttons, text fields)
 * - medium: Cards, dialogs
 * - large: Large surfaces, sheets
 * - extraLarge: Full-screen sheets, containers
 *
 * Usage:
 * ```
 * Card(
 *     shape = MaterialTheme.shapes.medium
 * )
 * ```
 */
internal val WalletShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
