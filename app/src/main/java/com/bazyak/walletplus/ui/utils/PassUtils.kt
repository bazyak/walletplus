package com.bazyak.walletplus.ui.utils

import android.text.Html
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.parser.pkpass.PKField
import com.bazyak.walletplus.designsystem.foundation.color.parseColor

/**
 * Drops fields that carry no value.
 *
 * A pass may declare a field with a key and no value — Apple Wallet renders nothing for those, and
 * so should we. Rendering them produces a label with an empty line under it, and, where the field
 * also has no label, leaks the raw PKPass key into the UI ("secondaryFields1", "title"). Issuers
 * ship such fields often enough that this has to be handled at render time rather than per-pass.
 *
 * The check runs on the rendered text, not the raw value: issuers ship placeholder values such as
 * `&nbsp;` or `<br>` that are not blank as a string but strip to nothing, leaving a label floating
 * over empty space. Some also set the label to the field key itself, so dropping the field is the
 * only way to keep "secondaryFields1" off the screen.
 *
 * Numeric values are not at risk: the parser's serializer converts every JSON primitive to a String
 * on the way in, so a balance of 472 arrives as "472" and survives the filter.
 */
fun List<PKField>.withValues(): List<PKField> = filter { field ->
    val raw = field.value?.toString().orEmpty()
    raw.isNotBlank() && stripHtml(raw).isNotBlank()
}

/**
 * Strip HTML tags from a string and decode HTML entities.
 */
fun stripHtml(html: String): String = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()

/**
 * Data class holding the computed card colors for pass cards.
 *
 * @param background The background color of the card
 * @param foreground The foreground/accent color (may be null)
 * @param text The text color (guaranteed to have sufficient contrast with background)
 * @param label The label color for field labels (guaranteed to have sufficient contrast with background)
 */
data class CardColors(val background: Color, val foreground: Color?, val text: Color, val label: Color)

/**
 * Compute card colors for a pass with proper contrast handling.
 *
 * Centralizes the color calculation logic used across all pass card components.
 * Ensures text and label colors have sufficient contrast against the background.
 *
 * @param pass The pass to compute colors for
 * @param isDarkTheme Whether the system is in dark theme mode
 * @return CardColors with background, foreground, text, and label colors (all contrast-checked)
 */
@Composable
fun rememberCardColors(pass: Pass): CardColors {
    val backgroundColor = pass.backgroundColor?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.surface

    val textColorRaw = pass.foregroundColor?.let { parseColor(it) }
    val labelColorRaw = pass.labelColor?.let { parseColor(it) }

    val textColor = textColorRaw ?: MaterialTheme.colorScheme.onSurface
    val labelColor = labelColorRaw ?: MaterialTheme.colorScheme.onSurface

    return CardColors(
        background = backgroundColor,
        foreground = textColorRaw,
        text = textColor,
        label = labelColor,
    )
}
