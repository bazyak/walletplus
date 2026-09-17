package com.bazyak.walletplus.data.exporter.pkpass

import com.bazyak.walletplus.data.model.Pass
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes a stored PKPass back into Apple's .pkpass ZIP shape.
 */
object PKPassZipWriter {
    fun write(pass: Pass, zipOut: ZipOutputStream) {
        addPassJson(zipOut, pass.rawData)

        val assetsFolder = File(pass.assetsDirectory)
        if (assetsFolder.exists() && assetsFolder.isDirectory) {
            addImages(zipOut, assetsFolder)
        }
    }

    private fun addPassJson(zipOut: ZipOutputStream, rawData: String) {
        val entry = ZipEntry("pass.json")
        zipOut.putNextEntry(entry)
        zipOut.write(rawData.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
    }

    private fun addImages(zipOut: ZipOutputStream, assetsFolder: File) {
        assetsFolder.listFiles()?.forEach { file ->
            if (file.isFile && file.extension.lowercase() == "png") {
                val entry = ZipEntry(file.name)
                zipOut.putNextEntry(entry)
                file.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }
}
