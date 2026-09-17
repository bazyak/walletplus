package com.bazyak.walletplus.data.exporter

import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassFormat
import java.io.File

/**
 * Interface for exporting passes to shareable files.
 * Mirrors the PassParser architecture for consistency.
 */
interface PassExporter {
    /**
     * Export a pass to a shareable file.
     * @param pass The pass to export
     * @return ExportResult containing the file and metadata, or null if export failed
     */
    suspend fun export(pass: Pass): ExportResult?

    /**
     * Get the list of formats this exporter supports.
     */
    fun getSupportedFormats(): List<PassFormat>

    /**
     * Check if this exporter can handle the given pass.
     */
    fun canExport(pass: Pass): Boolean
}

/**
 * Result of exporting a pass.
 */
data class ExportResult(
    /** The exported file ready for sharing */
    val file: File,

    /** MIME type for the exported file */
    val mimeType: String,

    /** Human-readable format name */
    val formatName: String,
)
