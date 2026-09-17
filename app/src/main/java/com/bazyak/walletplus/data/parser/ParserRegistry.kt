package com.bazyak.walletplus.data.parser

import android.content.Context
import android.net.Uri
import com.bazyak.walletplus.data.parser.pkpass.PKPassParser

/**
 * Factory pattern registry for resolving the correct parser based on file type.
 * Enables easy addition of new pass formats without modifying existing code.
 */
class ParserRegistry(context: Context) {
    private val parsers: List<PassParser> = listOf(
        PKPassParser(context),
        // Future parsers can be added here:
        // GoogleWalletParser(context),
        // CustomPassParser(context)
    )

    /**
     * Resolve the appropriate parser for the given URI and MIME type.
     * @param uri URI of the pass file
     * @param mimeType MIME type of the file (may be null)
     * @return PassParser that can handle the file, or null if no parser found
     */
    fun resolveParser(uri: Uri, mimeType: String?): PassParser? = parsers.firstOrNull { it.canParse(uri, mimeType) }

    /**
     * Get all registered parsers.
     */
    fun getAllParsers(): List<PassParser> = parsers
}
