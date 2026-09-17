package com.bazyak.walletplus.data.parser

import android.net.Uri
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassFormat

/**
 * Abstract parser interface for all pass formats.
 * Enables multimodal support for different pass types.
 */
interface PassParser {
    /**
     * Parse a pass file from the given URI.
     * @param uri URI of the pass file to parse
     * @return ParseResult containing the Pass entity and raw data, or null if parsing failed
     */
    suspend fun parse(uri: Uri): ParseResult?

    /**
     * Parse a pass file from the given URI to a temporary location for preview.
     * This is used when the user wants to preview a pass before adding it to the wallet.
     * @param uri URI of the pass file to parse
     * @return ParseResult containing the Pass entity with temp paths, or null if parsing failed
     */
    suspend fun parseToTemp(uri: Uri): ParseResult?

    /**
     * Get the list of formats this parser supports.
     */
    fun getSupportedFormats(): List<PassFormat>

    /**
     * Check if this parser can handle the given file.
     * @param uri URI of the file
     * @param mimeType MIME type of the file (may be null)
     * @return true if this parser can handle the file
     */
    fun canParse(uri: Uri, mimeType: String?): Boolean
}

/**
 * Result of parsing a pass file.
 */
data class ParseResult(
    /** The parsed Pass entity ready for database storage */
    val pass: Pass,

    /** Raw format-specific data (e.g., JSON string for PKPass) */
    val rawData: String,
)
