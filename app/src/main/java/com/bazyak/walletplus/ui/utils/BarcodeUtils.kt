package com.bazyak.walletplus.ui.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * Generate a barcode bitmap from a message string using ZXing.
 */
fun generateBarcodeBitmap(message: String, format: BarcodeFormat, width: Int = 600, height: Int = 300): Bitmap? = try {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        message,
        format,
        width,
        height,
    )

    val barcodeWidth = bitMatrix.width
    val barcodeHeight = bitMatrix.height
    val pixels = IntArray(barcodeWidth * barcodeHeight)

    for (y in 0 until barcodeHeight) {
        val offset = y * barcodeWidth
        for (x in 0 until barcodeWidth) {
            pixels[offset + x] = if (bitMatrix[x, y]) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
        }
    }

    Bitmap.createBitmap(barcodeWidth, barcodeHeight, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, barcodeWidth, 0, 0, barcodeWidth, barcodeHeight)
    }
} catch (e: Exception) {
    null
}

/**
 * Convert PKPass barcode format string to ZXing BarcodeFormat.
 */
fun pkPassFormatToZXingFormat(pkFormat: String): BarcodeFormat? = when (pkFormat) {
    "PKBarcodeFormatQR" -> BarcodeFormat.QR_CODE
    "PKBarcodeFormatPDF417" -> BarcodeFormat.PDF_417
    "PKBarcodeFormatAztec" -> BarcodeFormat.AZTEC
    "PKBarcodeFormatCode128" -> BarcodeFormat.CODE_128
    else -> null
}
