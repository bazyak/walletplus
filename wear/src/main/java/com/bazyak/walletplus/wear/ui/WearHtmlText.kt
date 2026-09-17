package com.bazyak.walletplus.wear.ui

import android.text.Html

internal fun String.toWearPlainText(): String {
    val html = replace("\n", "<br>")
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        .toString()
        .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
