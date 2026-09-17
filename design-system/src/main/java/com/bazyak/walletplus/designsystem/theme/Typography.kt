package com.bazyak.walletplus.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Complete Material 3 typography scale for the Wallet design system.
 *
 * Defines all text styles from the Material 3 type system:
 * - Display: Large expressive headlines
 * - Headline: High-emphasis text
 * - Title: Medium-emphasis text
 * - Body: Regular text content
 * - Label: Small UI elements like buttons
 *
 * Uses LINE Seed JP as the primary font family across all text styles.
 * For the app name branding, use AppNameTextStyle which uses the Ultra font.
 */
internal val WalletTypography = Typography(
    // Display styles - Large, expressive headlines
    displayLarge = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    // Headline styles - High-emphasis text
    headlineLarge = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // Title styles - Medium-emphasis text
    titleLarge = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Body styles - Regular text content
    bodyLarge = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // Label styles - Small UI elements (buttons, chips, etc.)
    labelLarge = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = LineSeedJp,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * Text style for the app name ".wallet+".
 *
 * Uses the same geometric sans as the rest of the app rather than a separate display face: at
 * wordmark size a heavy slab reads as decoration, while the interface font reads as a name.
 * Slight negative tracking keeps the short string from looking loose at this weight.
 */
val AppNameTextStyle = TextStyle(
    fontFamily = LineSeedJp,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp,
)
