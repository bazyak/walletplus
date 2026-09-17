package com.bazyak.walletplus.data.parser.pkpass

import android.util.Log

/**
 * Parser for Apple PKPass .strings files
 * Format: "key" = "value";
 * Supports comments (/* */, //) and escape sequences
 */
object PKPassStringsParser {
    private const val TAG = "PKPassStringsParser"

    /**
     * Parses an Apple .strings file content into a map of key-value pairs
     *
     * @param content The raw .strings file content
     * @return Map of localized string keys to values
     */
    fun parse(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()

        try {
            // Remove comments
            val withoutComments = removeComments(content)

            // Pattern to match "key" = "value";
            val pattern = Regex(""""([^"\\]*(?:\\.[^"\\]*)*)"\s*=\s*"([^"\\]*(?:\\.[^"\\]*)*)"\s*;""")

            pattern.findAll(withoutComments).forEach { match ->
                val key = unescapeString(match.groupValues[1])
                val value = unescapeString(match.groupValues[2])

                if (key.isNotEmpty()) {
                    result[key] = value
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing .strings file", e)
        }

        return result
    }

    /**
     * Removes C-style comments (both /* */ and //)
     */
    private fun removeComments(content: String): String {
        var result = content

        // Remove multi-line comments /* */
        result = result.replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")

        // Remove single-line comments //
        result = result.replace(Regex("""//.*"""), "")

        return result
    }

    /**
     * Unescapes common escape sequences in strings
     * Supports: \", \\, \n, \r, \t, \uXXXX
     */
    private fun unescapeString(escaped: String): String {
        var result = escaped

        // Handle unicode escapes first \uXXXX
        val unicodePattern = Regex("""\\u([0-9a-fA-F]{4})""")
        unicodePattern.findAll(escaped).forEach { match ->
            val codePoint = match.groupValues[1].toInt(16)
            result = result.replace(match.value, codePoint.toChar().toString())
        }

        // Handle standard escape sequences
        result = result.replace("""\\""", """\""")
        result = result.replace("""\"""", "\"")
        result = result.replace("""\n""", "\n")
        result = result.replace("""\r""", "\r")
        result = result.replace("""\t""", "\t")

        return result
    }
}
