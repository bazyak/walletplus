package com.bazyak.walletplus.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bazyak.walletplus.designsystem.R

/**
 * Custom font families for the Wallet design system.
 *
 * LineSeedJp: Main font family for all text content with multiple weights
 * UltraFamily: Display font for the app name and branding elements
 */
internal val LineSeedJp = FontFamily(
    Font(R.font.line_seed_jp_thin, FontWeight.Thin),
    Font(R.font.line_seed_jp_regular, FontWeight.Normal),
    Font(R.font.line_seed_jp_bold, FontWeight.Bold),
    Font(R.font.line_seed_jp_extrabold, FontWeight.ExtraBold),
)

internal val UltraFamily = FontFamily(
    Font(R.font.ultra_regular, FontWeight.Normal),
)
