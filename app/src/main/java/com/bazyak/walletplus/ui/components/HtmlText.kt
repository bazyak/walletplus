package com.bazyak.walletplus.ui.components

import android.text.Html
import android.text.Spanned
import android.text.style.URLSpan
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Composable that renders HTML text with clickable links.
 * Supports basic HTML tags: <a>, <b>, <i>, <br>
 */
@Composable
fun HtmlText(
    html: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    linkColor: Color = Color(0xFF2196F3),
) {
    val annotatedString = remember(html, color, linkColor) {
        parseHtmlToAnnotatedString(html, color, linkColor)
    }

    // Links are carried by LinkAnnotation inside the string, so a plain Text handles both the
    // styling and the click. ClickableText is deprecated and swallowed every gesture on the text,
    // which also made the card harder to interact with.
    Text(
        text = annotatedString,
        style = style.copy(color = color),
        modifier = modifier,
        softWrap = true,
    )
}

/**
 * Parse HTML string to AnnotatedString with formatting and clickable links.
 */
private fun parseHtmlToAnnotatedString(html: String, baseColor: Color, linkColor: Color): AnnotatedString {
    // Convert plain newlines to <br> tags before parsing
    // This preserves line breaks that aren't already in HTML format
    val processedHtml = html.replace("\n", "<br>")

    // Use Android's HTML parser with LEGACY mode to preserve more whitespace
    val spanned: Spanned = Html.fromHtml(processedHtml, Html.FROM_HTML_MODE_LEGACY)

    return buildAnnotatedString {
        append(spanned.toString())

        // Apply spans from HTML
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            when (span) {
                is URLSpan -> {
                    // A LinkAnnotation carries both the target and its styling, and Compose opens
                    // it through the platform UriHandler — no click handling needed at the call
                    // site, and an unopenable URL is ignored rather than crashing.
                    addLink(
                        url = LinkAnnotation.Url(
                            url = span.url,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ),
                        ),
                        start = start,
                        end = end,
                    )
                }
                is android.text.style.StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> {
                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.Bold),
                                start = start,
                                end = end,
                            )
                        }
                        android.graphics.Typeface.ITALIC -> {
                            addStyle(
                                style = SpanStyle(fontStyle = FontStyle.Italic),
                                start = start,
                                end = end,
                            )
                        }
                        android.graphics.Typeface.BOLD_ITALIC -> {
                            addStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                ),
                                start = start,
                                end = end,
                            )
                        }
                    }
                }
            }
        }
    }
}
