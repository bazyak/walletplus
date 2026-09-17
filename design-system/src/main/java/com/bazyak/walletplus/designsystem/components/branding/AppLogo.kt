package com.bazyak.walletplus.designsystem.components.branding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bazyak.walletplus.designsystem.theme.AppNameTextStyle

/** Accent used for the "+" in the wordmark. Matches the launcher icon. */
val AppLogoAccent = Color(0xFFFF7A00)

/**
 * App logo component displaying ".wallet+" text.
 *
 * Renders the app name using the Ultra font (AppNameTextStyle), with the trailing "+" in the
 * accent colour so the wordmark matches the launcher icon.
 *
 * Usage:
 * ```
 * AppLogo()
 * AppLogo(color = Color.White)
 * ```
 *
 * @param color The color of the ".wallet" part. If null, uses the default color from the text
 *   style. The "+" keeps its accent colour either way.
 * @param modifier Optional modifier for the text
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier, color: Color? = null) {
    val text = remember(color) {
        buildAnnotatedString {
            if (color != null) {
                withStyle(SpanStyle(color = color)) { append(".wallet") }
            } else {
                append(".wallet")
            }

            withStyle(SpanStyle(color = AppLogoAccent)) { append("+") }
        }
    }

    Text(
        text = text,
        style = AppNameTextStyle,
        modifier = modifier,
    )
}
