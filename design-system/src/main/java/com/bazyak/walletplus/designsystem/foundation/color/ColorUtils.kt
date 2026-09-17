package com.bazyak.walletplus.designsystem.foundation.color

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Parse hex color string to Color.
 *
 * Supports various hex formats:
 * - #RGB
 * - #RRGGBB
 * - #AARRGGBB
 *
 * @param colorString The hex color string to parse
 * @return The parsed Color, or Color.Unspecified if parsing fails
 */
fun parseColor(colorString: String): Color = try {
    Color(android.graphics.Color.parseColor(colorString))
} catch (e: Exception) {
    Color.Unspecified
}

/**
 * Ensure a color has sufficient contrast against a background.
 *
 * Validates the contrast ratio according to WCAG AA standards (4.5:1 for normal text).
 * If the foreground color doesn't meet the contrast requirement, returns the appropriate fallback.
 *
 * @param foregroundColor The foreground/text color to check
 * @param backgroundColor The background color to check against
 * @param isDarkTheme Whether the app is in dark theme mode
 * @param lightFallback Fallback color to use in light theme if contrast is insufficient
 * @param darkFallback Fallback color to use in dark theme if contrast is insufficient
 * @return The original foreground color if contrast is sufficient, otherwise the fallback color
 */
fun ensureContrast(
    foregroundColor: Color?,
    backgroundColor: Color?,
    isDarkTheme: Boolean,
    lightFallback: Color,
    darkFallback: Color,
): Color {
    if (foregroundColor == null || backgroundColor == null) {
        return if (isDarkTheme) darkFallback else lightFallback
    }

    val contrast = calculateContrastRatio(foregroundColor, backgroundColor)

    // WCAG AA standard requires 4.5:1 for normal text
    return if (contrast >= 4.5) {
        foregroundColor
    } else {
        if (isDarkTheme) darkFallback else lightFallback
    }
}

/**
 * Calculate contrast ratio between two colors.
 *
 * Uses the WCAG 2.0 contrast ratio formula:
 * (lighter + 0.05) / (darker + 0.05)
 *
 * Reference: https://www.w3.org/TR/WCAG20-TECHS/G17.html
 *
 * @param color1 The first color
 * @param color2 The second color
 * @return The contrast ratio (range: 1.0 to 21.0)
 */
fun calculateContrastRatio(color1: Color, color2: Color): Float {
    val lum1 = color1.luminance()
    val lum2 = color2.luminance()
    val lighter = maxOf(lum1, lum2)
    val darker = minOf(lum1, lum2)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/**
 * Blend two colors together with a given ratio.
 *
 * Performs linear interpolation between two colors.
 *
 * @param color1 The first color (starting color)
 * @param color2 The second color (ending color)
 * @param ratio How much to blend towards color2 (0.0 = all color1, 1.0 = all color2)
 * @return The blended color
 */
fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
    val clampedRatio = ratio.coerceIn(0f, 1f)
    val inverseRatio = 1f - clampedRatio

    return Color(
        red = color1.red * inverseRatio + color2.red * clampedRatio,
        green = color1.green * inverseRatio + color2.green * clampedRatio,
        blue = color1.blue * inverseRatio + color2.blue * clampedRatio,
        alpha = color1.alpha * inverseRatio + color2.alpha * clampedRatio,
    )
}

/**
 * Create a linear gradient brush for custom pass cards.
 *
 * Creates a subtle gradient from the background color (top-left) to a slightly darker
 * blended color (bottom-right). The gradient adds depth to flat colored cards.
 *
 * @param backgroundColor The pastel background color
 * @param foregroundColor The darker foreground/text color
 * @return A linear gradient Brush from backgroundColor to a darker blended variant
 */
fun createCustomPassGradient(backgroundColor: Color, foregroundColor: Color): Brush {
    val darkerColor = blendColors(backgroundColor, foregroundColor, 0.2f)
    return Brush.linearGradient(
        colors = listOf(
            backgroundColor, // Lighter at top-left
            darkerColor, // Darker at bottom-right
        ),
        start = Offset(0f, 0f),
        end = Offset.Infinite,
    )
}
