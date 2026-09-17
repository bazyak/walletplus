package com.bazyak.walletplus.data.exporter

import android.content.Context
import com.bazyak.walletplus.data.exporter.pkpass.PKPassExporter
import com.bazyak.walletplus.data.model.Pass

/**
 * Factory pattern registry for resolving the correct exporter based on pass format.
 * Enables easy addition of new export formats without modifying existing code.
 */
class ExporterRegistry(context: Context) {
    private val exporters: List<PassExporter> = listOf(
        PKPassExporter(context),
        // Future exporters can be added here:
        // GoogleWalletExporter(context),
    )

    /**
     * Resolve the appropriate exporter for the given pass.
     */
    fun resolveExporter(pass: Pass): PassExporter? = exporters.firstOrNull { it.canExport(pass) }

    /**
     * Get all registered exporters.
     */
    fun getAllExporters(): List<PassExporter> = exporters
}
