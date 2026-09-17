package com.bazyak.walletplus.data.archive

import android.content.Context
import android.net.Uri
import com.bazyak.walletplus.data.exporter.ExportResult
import com.bazyak.walletplus.data.exporter.pkpass.PKPassZipWriter
import com.bazyak.walletplus.data.json.WalletJson
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassCategory
import com.bazyak.walletplus.data.model.PassFormat
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.asException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object WalletArchive {
    const val MIME_TYPE = "application/zip"
    private const val VERSION = 1
    private const val MANIFEST_ENTRY = "manifest.json"

    fun createExportFile(cacheDir: File): File {
        val outputDir = File(cacheDir, "shared_passes")
        outputDir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(outputDir, "wallet_backup_$timestamp.zip")
    }

    fun export(passes: List<Pass>, outputFile: File): ExportResult {
        outputFile.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            val exportablePasses = passes.filter { it.format == PassFormat.PKPASS || it.format == PassFormat.CUSTOM }
            val manifest = WalletArchiveManifest(
                exportedAt = System.currentTimeMillis(),
                passes = exportablePasses.map { pass ->
                    WalletArchiveManifestPass(
                        id = pass.id,
                        format = pass.format,
                        displayOrder = pass.displayOrder,
                    )
                },
            )

            zipOut.addTextEntry(MANIFEST_ENTRY, WalletJson.json.encodeToString(manifest))

            exportablePasses.forEach { pass ->
                val passDir = "passes/${pass.id}/"
                zipOut.addTextEntry("${passDir}metadata.json", WalletJson.json.encodeToString(pass.toMetadata()))

                if (pass.format == PassFormat.PKPASS) {
                    zipOut.putNextEntry(ZipEntry("${passDir}pass.pkpass"))
                    ZipOutputStream(zipOut.nonClosing()).use { pkPassOut ->
                        PKPassZipWriter.write(pass, pkPassOut)
                    }
                    zipOut.closeEntry()
                }
            }
        }

        return ExportResult(
            file = outputFile,
            mimeType = MIME_TYPE,
            formatName = "Wallet Backup",
        )
    }

    fun read(inputStream: InputStream): WalletArchiveReadResult {
        val entries = mutableMapOf<String, ByteArray>()

        ZipInputStream(inputStream).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                val entryName = entry.name
                val normalizedEntryName = entryName.trimEnd('/')
                if (!isSafeEntryName(normalizedEntryName)) {
                    failWith(WalletError.UnsafeEntry(entryName))
                }
                if (!entry.isDirectory) {
                    entries[entryName] = zipIn.readBytes()
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        val manifestBytes = entries.requiredEntry(
            key = MANIFEST_ENTRY,
            error = WalletError.MissingManifest,
        )
        val manifest = WalletJson.json.decodeFromString<WalletArchiveManifest>(
            manifestBytes.toString(Charsets.UTF_8),
        )

        if (manifest.version != VERSION) {
            failWith(WalletError.UnsupportedVersion(manifest.version))
        }
        if (manifest.passes.isEmpty()) {
            failWith(WalletError.EmptyArchive)
        }

        val archivePasses = manifest.passes.map { manifestPass ->
            val passDir = "passes/${manifestPass.id}/"
            val metadataBytes = entries.requiredEntry(
                key = "${passDir}metadata.json",
                error = WalletError.MissingMetadata(manifestPass.id),
            )
            val metadata = WalletJson.json.decodeFromString<WalletArchivePassMetadata>(
                metadataBytes.toString(Charsets.UTF_8),
            )
            if (metadata.id != manifestPass.id || metadata.format != manifestPass.format) {
                failWith(WalletError.MetadataMismatch(manifestPass.id))
            }

            WalletArchivePassPayload(
                metadata = metadata,
                pkPassBytes = entries["${passDir}pass.pkpass"],
            )
        }

        return WalletArchiveReadResult(archivePasses)
    }

    fun isWalletArchiveUri(context: Context, uri: Uri): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType == MIME_TYPE ||
            mimeType == "application/x-zip-compressed" ||
            uri.path?.endsWith(".zip", ignoreCase = true) == true ||
            uri.toString().endsWith(".zip", ignoreCase = true)
    }

    fun isSafeEntryName(entryName: String): Boolean = entryName.isNotBlank() &&
        !entryName.startsWith("/") &&
        !entryName.startsWith("\\") &&
        !entryName.contains("\\") &&
        entryName.split('/').none { segment -> segment == ".." || segment.isBlank() }

    private fun Map<String, ByteArray>.requiredEntry(key: String, error: WalletError): ByteArray =
        this[key] ?: throw error.asException()

    private fun failWith(error: WalletError): Nothing = throw error.asException()

    private fun Pass.toMetadata(): WalletArchivePassMetadata = WalletArchivePassMetadata(
        id = id,
        format = format,
        organizationName = organizationName,
        description = description,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        labelColor = labelColor,
        rawData = rawData,
        importedDate = importedDate,
        lastRefreshDate = lastRefreshDate,
        autoRefreshEnabled = autoRefreshEnabled,
        displayOrder = displayOrder,
        category = category,
    )

    fun WalletArchivePassMetadata.toCustomPass(): Pass = Pass(
        id = id,
        format = PassFormat.CUSTOM,
        organizationName = organizationName,
        description = description,
        iconPath = "",
        logoPath = null,
        stripPath = null,
        backgroundPath = null,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        labelColor = labelColor,
        assetsDirectory = "",
        rawData = rawData,
        importedDate = importedDate,
        lastRefreshDate = lastRefreshDate,
        autoRefreshEnabled = false,
        displayOrder = displayOrder,
        category = category,
    )

    private fun ZipOutputStream.addTextEntry(name: String, value: String) {
        putNextEntry(ZipEntry(name))
        write(value.toByteArray(Charsets.UTF_8))
        closeEntry()
    }
}

@Serializable
data class WalletArchiveManifest(
    val version: Int = 1,
    val exportedAt: Long,
    val passes: List<WalletArchiveManifestPass>,
)

@Serializable
data class WalletArchiveManifestPass(val id: String, val format: PassFormat, val displayOrder: Int)

@Serializable
data class WalletArchivePassMetadata(
    val id: String,
    val format: PassFormat,
    val organizationName: String,
    val description: String,
    val foregroundColor: String?,
    val backgroundColor: String?,
    val labelColor: String?,
    val rawData: String,
    val importedDate: Long,
    val lastRefreshDate: Long?,
    val autoRefreshEnabled: Boolean,
    val displayOrder: Int,
    val category: PassCategory,
)

data class WalletArchivePassPayload(val metadata: WalletArchivePassMetadata, val pkPassBytes: ByteArray?)

data class WalletArchiveReadResult(val passes: List<WalletArchivePassPayload>)

data class WalletArchiveImportResult(val importedCount: Int, val replacedCount: Int, val failedCount: Int) {
    val summaryMessage: String
        get() = buildString {
            append("Imported ")
            append(importedCount)
            append(if (importedCount == 1) " pass" else " passes")
            if (replacedCount > 0) {
                append(" (")
                append(replacedCount)
                append(" replaced)")
            }
            if (failedCount > 0) {
                append(", ")
                append(failedCount)
                append(" failed")
            }
        }
}

private fun ZipOutputStream.nonClosing(): java.io.OutputStream = object : java.io.OutputStream() {
    override fun write(b: Int) {
        this@nonClosing.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        this@nonClosing.write(b, off, len)
    }

    override fun close() {
        finish()
    }

    private fun finish() {
        // Intentionally do not close the outer wallet ZIP stream.
    }
}

private fun ZipInputStream.readBytes(): ByteArray {
    val buffer = ByteArrayOutputStream()
    copyTo(buffer)
    return buffer.toByteArray()
}
