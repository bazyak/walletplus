package com.bazyak.walletplus.designsystem.foundation.typography

import androidx.compose.ui.text.font.FontFamily

/**
 * Font families for the Wallet design system.
 *
 * Currently uses FontFamily.Default (system font).
 *
 * To add custom fonts:
 * 1. Place font files (.ttf or .otf) in design-system/src/main/res/font/
 * 2. Define the FontFamily:
 *    ```
 *    val InterFontFamily = FontFamily(
 *        Font(R.font.inter_regular, FontWeight.Normal),
 *        Font(R.font.inter_medium, FontWeight.Medium),
 *        Font(R.font.inter_semibold, FontWeight.SemiBold),
 *        Font(R.font.inter_bold, FontWeight.Bold)
 *    )
 *    ```
 * 3. Update Typography.kt to use the custom font family
 */

/**
 * Default font family for the design system.
 *
 * Currently uses the system default font.
 * Replace with a custom FontFamily when custom fonts are added.
 */
val DefaultFontFamily: FontFamily = FontFamily.Default

/**
 * Example of how to add a custom font family:
 *
 * ```
 * import androidx.compose.ui.text.font.Font
 * import androidx.compose.ui.text.font.FontWeight
 *
 * val InterFontFamily = FontFamily(
 *     Font(R.font.inter_regular, FontWeight.Normal),
 *     Font(R.font.inter_medium, FontWeight.Medium),
 *     Font(R.font.inter_semibold, FontWeight.SemiBold),
 *     Font(R.font.inter_bold, FontWeight.Bold)
 * )
 * ```
 */
