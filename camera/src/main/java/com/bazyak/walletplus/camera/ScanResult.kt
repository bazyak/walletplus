package com.bazyak.walletplus.camera

/**
 * Sealed interface representing the result of a barcode scan.
 * Used for type-safe communication between camera module and app module.
 */
sealed interface ScanResult {
    /**
     * A URL was detected (http:// or https://)
     * Should trigger download flow for .pkpass file
     */
    data class UrlDetected(val url: String) : ScanResult

    /**
     * A barcode was detected (non-URL)
     * Should trigger custom pass builder flow
     */
    data class BarcodeDetected(
        val value: String,
        val format: Int // com.google.mlkit.vision.barcode.common.Barcode.FORMAT_*
    ) : ScanResult
}
