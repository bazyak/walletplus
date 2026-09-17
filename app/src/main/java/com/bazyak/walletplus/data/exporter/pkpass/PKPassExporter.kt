package com.bazyak.walletplus.data.exporter.pkpass

import android.content.Context
import com.bazyak.walletplus.data.exporter.ExportResult
import com.bazyak.walletplus.data.exporter.PassExporter
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassFormat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exporter for PKPass (Apple Wallet) format.
 * Reconstructs a .pkpass ZIP file from stored pass data and assets.
 */
class PKPassExporter(private val context: Context) : PassExporter {

    override suspend fun export(pass: Pass): ExportResult? = withContext(Dispatchers.IO) {
        try {
            // Validate format
            if (!canExport(pass)) {
                return@withContext null
            }

            // Validate required data
            if (pass.rawData.isBlank()) {
                return@withContext null
            }

            // Create output file
            val outputFile = createOutputFile(pass)
            outputFile.parentFile?.mkdirs()

            // Create ZIP archive
            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                PKPassZipWriter.write(pass, zipOut)
            }

            ExportResult(
                file = outputFile,
                mimeType = "application/vnd.apple.pkpass",
                formatName = "PKPass",
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getSupportedFormats(): List<PassFormat> = listOf(PassFormat.PKPASS)

    override fun canExport(pass: Pass): Boolean = pass.format == PassFormat.PKPASS

    /**
     * Create output file in cache directory with sanitized filename.
     */
    private fun createOutputFile(pass: Pass): File {
        val cacheDir = File(context.cacheDir, "shared_passes")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val organizationName = sanitizeFileName(pass.organizationName ?: "pass")
        val fileName = "${organizationName}_$timestamp.pkpass"
        return File(cacheDir, fileName)
    }

    /**
     * Sanitize filename by removing/replacing invalid characters.
     */
    private fun sanitizeFileName(name: String): String = name
        .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        .take(50) // Limit length
        .trim('_')
        .ifEmpty { "pass" }
}
