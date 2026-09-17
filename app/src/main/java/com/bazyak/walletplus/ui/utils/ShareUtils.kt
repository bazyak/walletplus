package com.bazyak.walletplus.ui.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.data.exporter.ExportResult

/**
 * Share a pass file using the system share sheet.
 * Uses ExportResult to get proper MIME type and file.
 */
fun sharePassFile(context: Context, exportResult: ExportResult) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "com.bazyak.walletplus.fileprovider",
            exportResult.file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = exportResult.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val formatName = if (exportResult.formatName == "Wallet Backup") {
            context.getString(R.string.wallet_backup)
        } else {
            exportResult.formatName
        }
        val chooserTitle = context.getString(R.string.share_format, formatName)
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
